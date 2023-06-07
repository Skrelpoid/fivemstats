package de.skrelpoid.fivemstats.data.service;

import org.springframework.stereotype.Service;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

/**
 * Converter between Long and String
 * @author dustinkristen
 *
 */
@Service
public class LongConverter implements Converter<String, Long> {
	private static final long serialVersionUID = 1L;

	@Override
	public Result<Long> convertToModel(final String value, final ValueContext context) {
		if (value == null || value.isBlank()) {
			return Result.ok(null);
		}
		try {
			return Result.ok(Long.parseLong(value));
		} catch (final NumberFormatException ex) {
			return Result.error(ex.getMessage());
		}
	}

	@Override
	public String convertToPresentation(final Long value, final ValueContext context) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}

}