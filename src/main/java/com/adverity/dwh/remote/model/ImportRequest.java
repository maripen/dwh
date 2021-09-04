package com.adverity.dwh.remote.model;

import com.adverity.dwh.annotation.ValidFileExtension;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
public class ImportRequest {
	@ValidFileExtension
	private URI url;
}
