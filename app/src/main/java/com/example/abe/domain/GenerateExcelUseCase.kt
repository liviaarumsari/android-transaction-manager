package com.example.abe.domain

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class GenerateExcelUseCase(
    private val newFormat: Boolean,
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val sheetName: String,
    private val headerList: List<String>,
    private val dataList: List<List<String>>
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.Default) {
            val workbook = createWorkbook()

            withContext(Dispatchers.IO) {
                try {
                    val outputStream = contentResolver.openOutputStream(uri)
                    workbook.write(outputStream)
                    outputStream?.close()
                    Log.d("ABE-EXPORT", "Finish writing file")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createWorkbook(): Workbook {
        val workbook = if (newFormat) XSSFWorkbook() else HSSFWorkbook()
        val sheet: Sheet = workbook.createSheet(sheetName)

        val cellStyle = getHeaderStyle(workbook)
        createSheetHeader(cellStyle, sheet)
        addData(1, sheet)

        return workbook
    }

    private fun createSheetHeader(cellStyle: CellStyle?, sheet: Sheet) {
        val row = sheet.createRow(0)

        for ((index, value) in headerList.withIndex()) {
            val columnWidth = (15 * 500)
            sheet.setColumnWidth(index, columnWidth)

            val cell = row.createCell(index)
            cell?.setCellValue(value)
            if (cellStyle != null)
                cell.cellStyle = cellStyle
        }
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle? {
        if (!newFormat) return null
        val cellStyle: CellStyle = workbook.createCellStyle()

        val colorMap: IndexedColorMap = (workbook as XSSFWorkbook).stylesSource.indexedColors
        val color = XSSFColor(IndexedColors.LIGHT_CORNFLOWER_BLUE, colorMap).indexed
        cellStyle.fillForegroundColor = color
        cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

        return cellStyle
    }


    private fun addData(initialRowIndex: Int, sheet: Sheet) {
        for ((rowIndex, rowList) in dataList.withIndex()) {
            val row = sheet.createRow(rowIndex + initialRowIndex)

            for ((colIndex, cellData) in rowList.withIndex()) {
                createCell(row, colIndex, cellData)
            }
        }
    }

    private fun createCell(row: Row, columnIndex: Int, value: String?) {
        val cell = row.createCell(columnIndex)
        cell?.setCellValue(value)
    }
}