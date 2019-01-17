package com.momdownloader.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.momdownloader.model.OrderFile;

public class FileManager {
	public static File createZipFile(String path, Map<OrderFile, String> filesToDownload) throws IOException{
		ZipEntry entry;
		byte[] data;
		
		File zipFile = new File(path + "MOMFiles.zip");
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		for (OrderFile file : filesToDownload.keySet()) {
			entry = new ZipEntry(file.getFileName());
			out.putNextEntry(entry);
			data = filesToDownload.get(file).getBytes();
			out.write(data, 0, data.length);
			out.closeEntry();
		}
		out.close();
		return zipFile;
	}
}