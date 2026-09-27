package com.example

import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", FormatUtils.formatBytes(0))
        assertEquals("500.00 B", FormatUtils.formatBytes(500))
        assertEquals("1.00 KB", FormatUtils.formatBytes(1024))
        assertEquals("1.50 MB", FormatUtils.formatBytes(1572864))
        assertEquals("10.00 GB", FormatUtils.formatBytes(10737418240L))
    }

    @Test
    fun testFormatSpeed() {
        assertEquals("2.50 MB/s", FormatUtils.formatSpeed(2621440L))
    }

    @Test
    fun testFormatEta() {
        assertEquals("--", FormatUtils.formatEta(0))
        assertEquals("8s", FormatUtils.formatEta(8))
        assertEquals("1m 15s", FormatUtils.formatEta(75))
    }

    @Test
    fun testGenerateFileName() {
        val fileName = FormatUtils.generateFileName(
            template = "%(uploader)s_%(id)s.%(ext)s",
            uploader = "CyberChannel",
            id = "vid12345",
            title = "Awesome Video",
            ext = "mp4"
        )
        assertEquals("CyberChannel_vid12345.mp4", fileName)
    }

    @Test
    fun testGenerateFileNameSanitization() {
        val fileName = FormatUtils.generateFileName(
            template = "%(title)s.%(ext)s",
            uploader = "artist",
            id = "1",
            title = "Illegal/Chars:Name*Test?Yes",
            ext = "mp4"
        )
        assertTrue(!fileName.contains("/"))
        assertTrue(!fileName.contains("*"))
        assertTrue(!fileName.contains("?"))
        assertTrue(fileName.endsWith(".mp4"))
    }
}
