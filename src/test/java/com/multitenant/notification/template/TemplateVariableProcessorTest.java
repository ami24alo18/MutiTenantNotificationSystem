package com.multitenant.notification.template;

import com.multitenant.notification.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateVariableProcessorTest {

	private TemplateVariableProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new TemplateVariableProcessor();
	}

	@Test
	void extractVariablesFromSubjectAndBody() {
		List<String> variables = processor.extractVariables(
				"Hello {{ firstName }}",
				"Order {{orderId}} for {{firstName}}"
		);

		assertThat(variables).containsExactly("firstName", "orderId");
	}

	@Test
	void rejectsMalformedPlaceholders() {
		assertThatThrownBy(() -> processor.extractVariables(null, "Hi {{ }}"))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Invalid template placeholder");
	}

	@Test
	void renderSubstitutesValues() {
		String rendered = processor.render(
				"Hello {{name}}, your code is {{code}}",
				Map.of("name", "Ada", "code", "42")
		);

		assertThat(rendered).isEqualTo("Hello Ada, your code is 42");
	}

	@Test
	void renderFailsWhenVariableMissing() {
		assertThatThrownBy(() -> processor.render("Hi {{name}}", Map.of()))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Missing value for template variable: name");
	}

	@Test
	void jsonRoundTrip() {
		String json = processor.toJsonArray(List.of("a", "b"));
		assertThat(json).isEqualTo("[\"a\",\"b\"]");
		assertThat(processor.fromJsonArray(json)).containsExactly("a", "b");
	}
}
