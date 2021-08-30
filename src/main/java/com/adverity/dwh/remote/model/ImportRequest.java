package com.adverity.dwh.remote.model;

import com.adverity.dwh.annotation.ValidFileExtension;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.net.URI;

@Data
@NoArgsConstructor
public class ImportRequest {
	@ValidFileExtension
	private URI url;
}
