package de.ceskilia.schoolbot.school.timetable.util

import com.jakewharton.picnic.*
import de.ceskilia.schoolbot.school.timetable.Timetable

fun createTable(lessons: List<Timetable.Lesson>): String {
    val builder = TableSection.Builder()

    lessons.forEach {
        builder.addRow(Row.Builder()
            .addCell(it.course)
            .addCell(it.hours)
            .addCell(it.subject)
            .addCell(it.teacher)
            .addCell(it.room)
            .addCell(Cell.Builder(it.information)
                .setStyle(CellStyle.Builder()
                    .setAlignment(TextAlignment.MiddleLeft)
                    .build())
                .build())
            .build()
        )
    }

    return Table.Builder()
        .setBody(builder.build())
        .setCellStyle(CellStyle.Builder()
            .setAlignment(TextAlignment.MiddleCenter)
            .setPaddingLeft(1)
            .setPaddingRight(1)
            .setBorder(true)
            .build())
        .build()
        .renderText(border = TextBorder.ASCII)
}