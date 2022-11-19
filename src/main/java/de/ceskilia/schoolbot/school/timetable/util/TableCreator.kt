package de.ceskilia.schoolbot.school.timetable.util

import com.jakewharton.picnic.*
import de.ceskilia.schoolbot.school.timetable.Timetable

val informationCellStyle = CellStyle.Builder()
    .setAlignment(TextAlignment.MiddleLeft)
    .build()
val defaultCellStyle = CellStyle.Builder()
    .setAlignment(TextAlignment.MiddleCenter)
    .setPaddingLeft(1)
    .setPaddingRight(1)
    .setBorder(true)
    .build()

fun createTable(lessons: List<Timetable.Lesson>): String {
    val builder = TableSection.Builder()

    lessons.forEach {
        with(it) {
            builder.addRow(Row.Builder()
                .addCell(course)
                .addCell(hours)
                .addCell(subject)
                .addCell(teacher)
                .addCell(room)
                .addCell(Cell.Builder(information)
                    .setStyle(informationCellStyle)
                    .build())
                .build()
            )
        }
    }

    return Table.Builder()
        .setBody(builder.build())
        .setCellStyle(defaultCellStyle)
        .build()
        .renderText(border = TextBorder.ASCII)
}