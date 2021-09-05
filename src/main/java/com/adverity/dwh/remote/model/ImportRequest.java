package com.adverity.dwh.remote.model;

import com.adverity.dwh.annotation.ValidFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
public class ImportRequest {
	@Schema(name = "url", description = "url of the csv data source")
	@ValidFileExtension
	private URI url;
}
