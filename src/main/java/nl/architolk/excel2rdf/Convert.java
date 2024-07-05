package nl.architolk.excel2rdf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.Runnable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "excel2rdf")
public class Convert implements Runnable{

  private static final String PROPERTY_NAMESPACE = "urn:property:";

  private static final Logger LOG = LoggerFactory.getLogger(Convert.class);

  @Option(names={"-i","-input"},description="Input file: <input.xls> or <input.xlsx>")
  private String inputFile;
  @Option(names={"-o","-output"},description="Output file: <output.xml> or <output.ttl> or..")
  private String outputFile;
  @Option(names={"-f","-format"},description="Serialization format to use in output")
  private String outputExt;

  @Parameters
  private List params;

  private Model outModel;
  private Resource workbookResource;

  @Override
  public void run() {
    if ((inputFile!=null) && (outputFile!=null)) {
      //New way of doing things, using real CLI parameters
      startConverting();
    } else {
      //Original parameter structure
      LOG.warn("Using deprecated way of stating parameters");
      if (params.size() == 2) {
        inputFile = (String)params.get(0);
        outputFile = (String)params.get(1);
        startConverting();
      } else {
        LOG.info("Usage: excel2rdf <input.xlsx> <output.ttl>");
      }
    }
  }

  static private RDFFormat getFormat(String outputExt) {
    RDFFormat format = null;
    if (outputExt!=null) {
      switch (outputExt) {
        case "RDFXML_PLAIN": format=RDFFormat.RDFXML_PLAIN; break;
      }
    }
    return format;
  }

  private String getCellValue(Cell cell) {
    CellStyle style = cell.getCellStyle();
    CellFormat cf = CellFormat.getInstance(style.getDataFormatString());
    CellFormatResult result = cf.apply(cell);
    return result.text;
  }

  private void convertSheet(int index, Sheet sheet) {
    LOG.info("Converting sheet: {}",sheet.getSheetName());

    Resource sheetResource = outModel.createResource("urn:excel:sheet"+index).addProperty(RDF.type, CSVW.Table).addProperty(RDFS.label,sheet.getSheetName());
    workbookResource.addProperty(CSVW.table,sheetResource);

    Boolean needFirstRow = true;

    Map<Integer,Property> properties = new HashMap<>();

    Iterator<Row> rows = sheet.rowIterator();
    while (rows.hasNext()) {
      Row row = rows.next();
      if (needFirstRow) {
        needFirstRow = false;
        for (Cell cell : row) {
          String colname = getCellValue(cell);
          if (!colname.isEmpty()) {
            properties.put(cell.getColumnIndex(),ResourceFactory.createProperty(PROPERTY_NAMESPACE,colname.replace(" ","_").replaceAll("[^a-zA-Z0-9_-]","")));
          }
        }
      } else {
        Resource subjectResource = outModel.createResource("urn:excel:sheet"+index+":row"+row.getRowNum()+"s");
        Resource rowResource = outModel.createResource("urn:excel:sheet"+index+":row"+row.getRowNum())
                                          .addProperty(RDF.type, CSVW.Row)
                                          .addProperty(CSVW.describes,subjectResource)
                                          .addLiteral(CSVW.rownum,row.getRowNum());
        for (Cell cell : row) {
          String value = getCellValue(cell);
          if (!value.isEmpty()) {
            Property property = properties.get(cell.getColumnIndex());
            if (property!=null) {
              subjectResource.addProperty(property,value);
            }
          }
        }

      }
    }

  }

  private void startConverting() {

    LOG.info("Starting conversion");
    LOG.info("Input file: {}",inputFile);
    LOG.info("Ouput file: {}",outputFile);

    try {

      FileInputStream fileIn = new FileInputStream(inputFile);
      Workbook wb = WorkbookFactory.create(fileIn);

      outModel = ModelFactory.createDefaultModel();

      outModel.setNsPrefix(CSVW.prefix,CSVW.namespace);
      outModel.setNsPrefix(RDFS.prefix,RDFS.namespace);
      outModel.setNsPrefix("xsd","http://www.w3.org/2001/XMLSchema#");
      workbookResource = outModel.createResource("urn:excel:workbook").addProperty(RDF.type, CSVW.TableGroup);

      for (int i = 0; i < wb.getNumberOfSheets(); i++) {
        convertSheet(i+1,wb.getSheetAt(i));
      }

      RDFFormat outputFormat = getFormat(outputExt);
      if (outputFormat==null) {
        RDFDataMgr.write(new FileOutputStream(outputFile),outModel, RDFLanguages.filenameToLang(outputFile,RDFLanguages.JSONLD));
      } else {
        RDFDataMgr.write(new FileOutputStream(outputFile),outModel, outputFormat);
      }

      LOG.info("Done!");
    }
    catch (Exception e) {
      LOG.error(e.getMessage(),e);
    }
  }

}
