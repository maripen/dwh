package com.adverity.dwh.remote.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.net.URI;

@Data
@NoArgsConstructor
public class ImportRequest {
	@NotNull
	private URI url;
}
