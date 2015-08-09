package org.springframework.web.filter.test;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

	@Override
	protected String generateETagHeaderValue(byte[] bytes) {
		final HashCode hash = Hashing.sha512().hashBytes(bytes);
		return "\"" + hash + "\"";
	}
}
