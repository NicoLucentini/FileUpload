package com.example.uploadingfiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestPart("file")MultipartFile file) {
		  try {
			  
			  Long size = file.getSize();
			  
			  
			  Long maxSize = 10240000L;
			  
			  byte[] bytes = file.getBytes();
			  
			  Long files =  (size / maxSize) + 1;
			  Long last = size % maxSize;
			  
			  
			  DiskItemFileFactory diff = new DiskItemFileFactory();
			  System.out.println(size);
			  System.out.println(last);
			  System.out.println(files);
			  
			  if(size > maxSize) {
				
				
				  for(int j = 0; j<files;j++)
				  {
					  File testFile = new File("test" + j);
					  
					  
					  byte[] partFile = new byte[j == files -1 ? last.intValue() : maxSize.intValue()];
					  
					  int start = j * maxSize.intValue();
					  int end =  j == files -1 ? start + last.intValue() : start + maxSize.intValue();
					  for(int i = start; i< (j==files-1 ? last :  maxSize);i++) {
						  partFile[i] = bytes[i];
					  }
					  FileUtils.writeByteArrayToFile(testFile, partFile);
					 
				  }
			  }
	        } catch (IOException e) {
	            e.printStackTrace();
	            return new ResponseEntity<String>("Failed", HttpStatus.INTERNAL_SERVER_ERROR);
	        }
		  
		  return new ResponseEntity<String>("Done", HttpStatus.OK);
    }

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
