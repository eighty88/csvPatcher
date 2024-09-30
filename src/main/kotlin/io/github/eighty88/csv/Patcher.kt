package io.github.eighty88.csv

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.system.exitProcess

class Patcher {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Patcher().init()
        }
    }

    private val logger: Logger = LoggerFactory.getLogger("Patcher")

    fun init() {
        val dir: File = File(System.getProperty("java.class.path")).absoluteFile.parentFile

        val input = File(dir, "input")
        val csv = File(dir, "csv")
        val output = File(dir, "out")

        val replace = mutableMapOf<String, String>()

        if (output.mkdir()) {
            logger.info("Create output directory.");
        }
        if (csv.mkdir()) {
            logger.info("Create csv directory.");
        }
        if (input.mkdir()) {
            logger.info("Create input directory.");
        }

        if (input.listFiles()?.size == 0) {
            logger.info("Input directory is empty.")
            logger.info("Stopping.")
            exitProcess(0)
        }

        csv.listFiles(object : FileFilter {
            override fun accept(file: File): Boolean {
                val fileName = file.getName()
                val i = fileName.lastIndexOf('.')
                if (i > 0 && i < fileName.length - 1) {
                    return fileName.substring(i + 1).lowercase(Locale.ENGLISH) == "csv"
                }
                return false
            }
        })?.forEach {
            replace.putAll(
                listReplace(it)
            )
        }

        replace.forEach { (k, v) -> logger.info("$k -> $v;") }

        input.listFiles()?.forEach {
            replace(it, File(output, it.name), replace)
        }
    }

    fun listReplace(file: File): Map<String, String> {
        val lines = file.readLines(StandardCharsets.UTF_8)

        val result = lines.filter { it.startsWith("field_") || it.startsWith("func_") || it.startsWith("p_") }
            .filter { it.contains(",0,") }
            .map { line -> line.split(",") }
            .map { lineArr -> lineArr[0] to lineArr[1] }.toMap()

        return result
    }

    fun replace(file: File, output: File, map: Map<String, String>) {
        var text = file.readText(StandardCharsets.UTF_8)

        map.forEach { (k, v) -> text = text.replace(k, v) }

        output.writeText(text, StandardCharsets.UTF_8)
    }
}