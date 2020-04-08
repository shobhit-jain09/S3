package com.example.newdemo.service;

import java.io.File;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	
	String uploadFile(MultipartFile file);

	void uploadFiles(List<MultipartFile> files);
	File download();
	
}
