package com.example.newdemo.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.hibernate.boot.archive.spi.ArchiveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.newdemo.entity.FileUploadEntity;
import com.example.newdemo.repo.FileUploadRepository;
import com.example.newdemo.service.FileService;
import com.example.newdemo.utility.FileUtility;

@Service
public class FileServiceImpl implements FileService {
	
	@Autowired
	private AmazonS3Client amazonClient;
	
	@Autowired
	private FileUploadRepository fileUploadRepository;
	

	@Value("${app.awsServices.bucketName}")
	private String bucketName;
	
	@Value("${app.fileUploadLocation}")
	private String fileUploadLocation;

	@Override
	public String uploadFile(MultipartFile file) {

		 String originalFileName = file.getOriginalFilename();
		    try {
		    	
			Path fileStorageLocation = Paths.get(fileUploadLocation).toAbsolutePath().normalize();
			String fileName = FileUtility.getFileNameCheck(fileStorageLocation, file);
			Path targetLocation = fileStorageLocation.resolve(fileName);
						
			Files.copy(file.getInputStream(), targetLocation);
			
			Timestamp rightNow = new Timestamp(new java.util.Date().getTime());
			
			FileUploadEntity fileUploadEntity = new FileUploadEntity();
			fileUploadEntity.setCreatedOn(rightNow);
			fileUploadEntity.setFileName(fileName);
			fileUploadEntity.setFileLocation(targetLocation.toString());
			fileUploadEntity.setOwnerId(new Long(1));
			
			fileUploadRepository.save(fileUploadEntity);
		     
		    } 
		    catch (FileAlreadyExistsException e) {
				// TODO: handle exception
		    	System.out.println(e);
			}catch (IOException ex) {
		      //LOG.error("Error in storing file: ", ex);
		      System.out.println(ex);
		    }
			return "File uploaded successfully!! : "+originalFileName;	
	}

	@Override
	public void uploadFiles(List<MultipartFile> files) {
		
			files.forEach(muitipartFile ->{
				File file = null;
				try {
					file = FileUtility.convertMultiPartToFile(muitipartFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				String fileName = FileUtility.generateFileName(muitipartFile, "vivek.dubey@xoriant.com");
				System.out.println("filename:"+fileName);
				uploadToS3(bucketName, fileName, file);
			});
	}
	

 

	private void uploadToS3(String bucketName, String fileName, File file) {
				
		PutObjectResult  t=	amazonClient.putObject(bucketName, fileName, file);
		t.getETag();
		System.out.println(t.getSSECustomerKeyMd5());
		Timestamp rightNow = new Timestamp(new java.util.Date().getTime());

		FileUploadEntity fileUploadEntity = new FileUploadEntity();
		fileUploadEntity.setCreatedOn(rightNow);
		fileUploadEntity.setFileName(file.getName());
		fileUploadEntity.setFileLocation(fileName);
		fileUploadEntity.setOwnerId(new Long(1));
		
		fileUploadRepository.save(fileUploadEntity);
		
	}

	
	
	private S3Object downloadS3bucket(String fileName,String bucketName) 
	 {
		 S3Object object =null;
		if (amazonClient != null && amazonClient.doesObjectExist(bucketName, fileName)) {
			object= amazonClient.getObject(bucketName,  fileName);
		        return object;
		}
		else
		{
			return object;
		}
	 }

	@Override
	public File download() {
		// TODO Auto-generated method stub
		S3ObjectInputStream iStream = null;
		File contentJson = null;
		String fileName="document/shobhit/1586335542184-lorax.jpg";
		S3Object object=downloadS3bucket(fileName,bucketName);
		if (object!=null)
		{
			iStream = object.getObjectContent();
			try {
				contentJson = unzip(iStream);
			} catch (ArchiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
			
		}
		return contentJson;
		
	}
	public static File unzip(InputStream in) throws IOException, ArchiveException {

		byte[] buffer = new byte[1024];
		GZIPInputStream gzis = null;
		FileOutputStream out = null;
		File temp = null;
		try {
		//	gzis = new GZIPInputStream(in);
			temp = File.createTempFile("", ".jpeg");

			// Delete temp file when program exits.
			//temp.deleteOnExit();

			// Write to temp file
			try {
				out = new FileOutputStream(temp);
				int len;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				return temp;
			} catch (IOException e) {
				//LOG.error("Error while writing temp File :" + e.getMessage(), e);
				if(temp != null)
					temp.delete();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				if (gzis != null)
					gzis.close();
			} catch (Exception e) {
				//LOG.error("Error closing GZIPInputStream ", e);
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				//LOG.error("Error closing FileOutputStream ", e);
			}
		}

		return null;
	}
	
}