package apple.util.rechercherExpression.excelGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelGenerator {
	private File _fDirUser;
	private File _fRechExpCollective;
	private File _fFileExcel;
	private Workbook _workbook;
	private Sheet _worksheet;
	private String _userId;
	private int width = 0;
	public int iCurrentCol = 0;
	public int iCurrentRow = 2;
	private Row currentRow;
	private ConnectionData _ctxData;

	private static final MsgId MSG_250_6018 = new MsgId("MSG_250_6018"); // Recherche Expression COLLECTIVE

	private static final MsgId MSG_250_5490 = new MsgId("MSG_250_5490"); // Date :
	private static final MsgId MSG_250_5491 = new MsgId("MSG_250_5491"); // Heure :
	private static final MsgId MSG_250_5489 = new MsgId("MSG_250_5489"); // Modèle :
	private static final MsgId MSG_250_6017 = new MsgId("MSG_250_6017"); // Expression Recherchée :

	private static final MsgId MSG_060_4812 = new MsgId("MSG_060_4812"); // Portef.
	private static final MsgId MSG_250_0261 = new MsgId("MSG_250_0261"); // Libellé Portefeuille
	private static final MsgId MSG_060_4809 = new MsgId("MSG_060_4809"); // Code Client
	private static final MsgId MSG_250_0263 = new MsgId("MSG_250_0263"); // Libellé Client
	private static final MsgId MSG_250_0453 = new MsgId("MSG_250_0453"); // ALP
	private static final MsgId MSG_250_0264 = new MsgId("MSG_250_0264"); // Mode Service
	private static final MsgId MSG_250_5308 = new MsgId("MSG_250_5308"); // Statut
	private static final MsgId MSG_032_0045 = new MsgId("MSG_032_0045"); // Dernier saveinit
	private static final MsgId MSG_250_0272 = new MsgId("MSG_250_0272"); // ECP
	private static final MsgId MSG_001_0056 = new MsgId("MSG_001_0056"); // Projet
	private static final MsgId MSG_060_3012 = new MsgId("MSG_060_3012"); // Code Rubrique
	private static final MsgId MSG_060_4630 = new MsgId("MSG_060_4630"); // Libellé Rubrique
	private static final MsgId MSG_230_0190 = new MsgId("MSG_230_0190"); // Rétro
	private static final MsgId MSG_230_0471 = new MsgId("MSG_230_0471"); // Code Rubrique

	private static final MsgId MSG_250_6019 = new MsgId("MSG_250_6019"); // Phase concernée

	public ExcelGenerator(ConnectionData ctx, String userId, String modeleId, String sExpRech, String sRules) throws PEAR_EXCEPTION_CO {
		_userId = userId;
		_ctxData = ctx;
		createFile(); // Création du fichier
		buildWhiteCanva(modeleId, sExpRech, sRules); // Création de l'entête
		currentRow = _worksheet.createRow(iCurrentRow);
	}

	/**
	 * Déplace le pointeur de ligne
	 * 
	 * @param index
	 */
	public void setRowPointer(int index) {
		iCurrentRow = index;
	}

	/**
	 * Remise à Zéro du pointeur de colonne
	 */
	public void resetColPointer() {
		iCurrentCol = 0;
	}

	/**
	 * Génére les pointeurs pour écrire sur une nouvelle ligne
	 */
	public void newLine() {
		currentRow = _worksheet.createRow(iCurrentRow++);
		iCurrentCol = 0;

	}

	/**
	 * Ajoute une cellule de texte
	 * 
	 * @param content
	 */
	public void addCell(String content) {
		CellStyle styleValeur = _workbook.createCellStyle();
		Font fontNormal = _workbook.createFont();
		fontNormal.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		styleValeur.setFont(fontNormal);
		styleValeur.setBorderLeft(CellStyle.BORDER_THIN);
		styleValeur.setBorderRight(CellStyle.BORDER_THIN);
		styleValeur.setBorderTop(CellStyle.BORDER_THIN);
		styleValeur.setBorderBottom(CellStyle.BORDER_THIN);

		Cell cell = currentRow.createCell(iCurrentCol++);
		cell.setCellValue(content);
		cell.setCellStyle(styleValeur);
	}

	/**
	 * Créé le fichier brute
	 * 
	 * @throws PEAR_EXCEPTION_CO
	 */
	private void createFile() throws PEAR_EXCEPTION_CO {
		try {
			String tempDirName = AppleProperties.getProperty("SHARED_DIR", null);
			if (tempDirName == null) {
				tempDirName = AppleProperties.getProperty("APPLE_WORK_PATH");
			}

			_fRechExpCollective = new File(tempDirName, "RechercheExpressionCollective");
			_fDirUser = new File(_fRechExpCollective, _userId);
			if (!_fDirUser.exists()) {
				_fDirUser.mkdirs();
			}
		} catch (Throwable e) {
			throw new PEAR_EXCEPTION_CO(PEAR_EXCEPTION_CO.ERREUR_CREATION_FICHIER, e,
					new String[] { _fDirUser.getPath() });
		}

		char cModel;
		String sFileExt = ".xlsx";
		String sModel = AppleProperties.getProperty("dossierclient.model", "xssf");
		if ("hssf".equalsIgnoreCase(sModel)) {
			cModel = 'H';
			sFileExt = ".xls";
		} else if ("sxssf".equalsIgnoreCase(sModel))
			cModel = 'S';
		else
			cModel = 'X';
		try {
			_fFileExcel = FileUtil.getNewTmpFile(_fDirUser, "REC" + "-", sFileExt, 128);
		} catch (IOException ex) {
			throw new PEAR_EXCEPTION_CO(PEAR_EXCEPTION_CO.ERREUR_CREATION_FICHIER, ex,
					new String[] { _fDirUser.getPath() });
		}
		if (cModel == 'H')
			_workbook = new HSSFWorkbook();
		else if (cModel == 'S')
			_workbook = new SXSSFWorkbook();
		else
			_workbook = new XSSFWorkbook();
	}

	/**
	 * Créé le canva de base du fichier Excel avec l'en-tête
	 * 
	 * @param sModele
	 * @param expRech
	 */
	public void buildWhiteCanva(String sModele, String expRech, String rules) {
		CellStyle styleEntete = _workbook.createCellStyle();
		Font fontGras = _workbook.createFont();
		fontGras.setBoldweight(Font.BOLDWEIGHT_BOLD);
		styleEntete.setFont(fontGras);
		styleEntete.setBorderLeft(CellStyle.BORDER_THIN);
		styleEntete.setBorderRight(CellStyle.BORDER_THIN);
		styleEntete.setBorderTop(CellStyle.BORDER_THIN);
		styleEntete.setBorderBottom(CellStyle.BORDER_THIN);

		CellStyle styleValeur = _workbook.createCellStyle();
		Font fontNormal = _workbook.createFont();
		fontNormal.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		styleValeur.setFont(fontNormal);
		styleValeur.setBorderLeft(CellStyle.BORDER_THIN);
		styleValeur.setBorderRight(CellStyle.BORDER_THIN);
		styleValeur.setBorderTop(CellStyle.BORDER_THIN);
		styleValeur.setBorderBottom(CellStyle.BORDER_THIN);

		_worksheet = _workbook.createSheet(AppleMessageCatalog.getMessageText(_ctxData, 
				new MsgId("MSG_250_6026"), "Recherche d'Expression Collective"));
		if (_worksheet instanceof SXSSFSheet)
			((SXSSFSheet) _worksheet).setRandomAccessWindowSize(-1);

		String sDateEtHeure = RecupDate.dateSystemeMillisecond();
		String sDate = sDateEtHeure.substring(8, 10) + "/" + sDateEtHeure.substring(5, 7) + "/"
				+ sDateEtHeure.substring(0, 4);
		String sHeure = sDateEtHeure.substring(11);

		int nRow = 0;
		Cell cell;

		// ligne d'info sur le lancement
		// index from 0,0... cell A1 is cell(0,0)
		Row row = _worksheet.createRow(nRow++);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_6018, "RECHERCHE EXPRESSION COLLECTIVE"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_5490, "Date : "));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(sDate);
		cell.setCellStyle(styleValeur);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_5491, "Heure : "));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(sHeure);
		cell.setCellStyle(styleValeur);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_5489, "Modèle : "));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(sModele);
		cell.setCellStyle(styleValeur);
		
		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, new MsgId("MSG_250_6029"), "Choix des règles :"));
		cell.setCellStyle(styleEntete);
		
		cell = row.createCell(width++);
		cell.setCellValue(rules);
		cell.setCellStyle(styleValeur);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_6017, "Expression Recherchée :"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(expRech);
		cell.setCellStyle(styleValeur);
		
		_worksheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 12));
		
		// FIN ligne d'info sur le lancement

		// ligne d'entête
		row = _worksheet.createRow(nRow++);
		width = 0;

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_060_4812, "Portef."));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_0261, "Libellé Portefeuille"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_060_4809, "Code Client"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_0263, "Libellé Client"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_0453, "ALP"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_0264, "Mode Service"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_5308, "Statut"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_032_0045, "Dernier SAVEINIT"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_0272, "ECP"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_001_0056, "Projet"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_060_3012, "Code Rubrique"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_060_4630, "Libellé Rubrique"));
		cell.setCellStyle(styleEntete);
		
		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_230_0190, "Rétro"));
		cell.setCellStyle(styleEntete);
		
		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_230_0471, "CSCP"));
		cell.setCellStyle(styleEntete);

		cell = row.createCell(width++);
		cell.setCellValue(AppleMessageCatalog.getMessageText(_ctxData, MSG_250_6019, "Phase concernée"));
		cell.setCellStyle(styleEntete);
		
		for (int i = 0; i < width; i++) {
			_worksheet.autoSizeColumn(i);
		}
	}

	/**
	 * S'assure d'avoir des cellules non nulles
	 * 
	 * @param worksheet
	 */
	private void ensureNoNullString(Sheet worksheet) {
		for (Row r : worksheet) {
			if (r != null) {
				for (Cell c : r) {
					if (c != null && c.getStringCellValue() == null)
						c.setCellValue("");
				}
			}
		}
	}

	/**
	 * Charge le fichier dans le système de fichier déclaré lors de la création du
	 * fichier
	 * 
	 * @return
	 * @throws Throwable
	 */
	public File loadFile() throws Throwable {
		if (_worksheet instanceof SXSSFSheet)
			ensureNoNullString(_worksheet);
		if (_worksheet instanceof SXSSFSheet) {
			try {
				((SXSSFSheet) _worksheet).flushRows();
			} catch (IOException e) {
				throw new PEAR_EXCEPTION_CO(e);
			}
		}
		FileOutputStream fileOut = new FileOutputStream(_fFileExcel);
		_workbook.write(fileOut);
		fileOut.flush();
		fileOut.close();
		fileOut = null;
		return _fFileExcel;

	}

}