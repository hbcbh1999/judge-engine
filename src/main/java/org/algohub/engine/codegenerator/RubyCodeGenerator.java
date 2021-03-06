package org.algohub.engine.codegenerator;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.algohub.engine.pojo.Function;
import org.algohub.engine.type.LanguageType;
import org.algohub.engine.util.ObjectMapperInstance;

/**
 * Generate runnable Ruby code and call user's function.
 */
@SuppressWarnings({"PMD.InsufficientStringBufferDeclaration"})
public final class RubyCodeGenerator {

  private RubyCodeGenerator() {}

  /**
   * Generate the main function.
   *
   * @param function Funciton prototype.
   * @return the complete source code.
   */
  @SuppressWarnings({"PMD.PreserveStackTrace"})
  public static String generateMain(final Function function) {
    final StringBuilder result = new StringBuilder();

    result.append("require 'json'\nrequire 'algohub'\nrequire_relative './solution'\n\n\n"
        + "if __FILE__ == $0\n");

    final Function.Parameter[] parameters = function.getParameters();
    try {
      for (final Function.Parameter parameter : parameters) {
        Indentation.append(result, parameter.getName() + "_type = Algohub::TypeNode.from_json('"
            + ObjectMapperInstance.INSTANCE.writeValueAsString(parameter.getType()) + "')\n", 1);

      }
      Indentation.append(result,
          "output_type = Algohub::TypeNode.from_json('" + ObjectMapperInstance.INSTANCE
              .writeValueAsString(function.getReturn_().getType()) + "')\n\n", 1);
    } catch (JsonProcessingException e) {  // impossible
      throw new IllegalStateException(e.getMessage());
    }

    Indentation.append(result, "raw_testcases = JSON.parse(STDIN.gets())\n\n", 1);

    Indentation.append(result, "(0..raw_testcases.size()-1).each do |i|\n", 1);
    Indentation.append(result, "test_case = raw_testcases[i]\n", 2);
    for (int i = 0; i < parameters.length; ++i) {
      final Function.Parameter parameter = parameters[i];
      Indentation.append(result,
          "algohub_" + parameter.getName() + " = Algohub.from_json(" + "test_case['input'][" + i
              + "], " + parameter.getName() + "_type)\n", 2);
    }

    Indentation.append(result,
        "expected_output = Algohub.from_json(" + "test_case['output'], output_type)\n\n", 2);

    Indentation.append(result, "actual_output = " + function.getName() + "(", 2);
    for (final Function.Parameter parameter : parameters) {
      result.append("algohub_").append(parameter.getName()).append(", ");
    }
    if (parameters.length > 0) {
      result.delete(result.length() - 2, result.length());
    }
    result.append(")\n\n");

    Indentation.append(result, "if actual_output != expected_output\n", 2);
    Indentation.append(result,
        "print(Algohub::JudgeResult.new(Algohub::StatusCode::WRONG_ANSWER).to_json())\n", 3);
    Indentation.append(result, "exit 0\n", 3);
    Indentation.append(result, "end\n", 2);
    Indentation.append(result, "end\n\n", 1);

    Indentation.append(result,
        "print(Algohub::JudgeResult.new(Algohub::StatusCode::ACCEPTED).to_json())\n", 1);
    result.append("end\n");
    return result.toString();
  }

  /**
   * Generate an empty function with comments.
   * @param function Function prototype
   * @return source code of a empty function
   */
  public static String generateEmptyFunction(final Function function) {
    final StringBuilder result = new StringBuilder();

    // function comment
    for (final Function.Parameter p : function.getParameters()) {
      result.append(
          "# @param {" + FunctionGenerator.generateTypeDeclaration(p.getType(), LanguageType.RUBY)
              + "} " + p.getName() + " " + p.getComment() + "\n");
    }

    final Function.Return return_ = function.getReturn_();
    result.append("# @return {" + FunctionGenerator.generateTypeDeclaration(return_.getType(),
        LanguageType.RUBY) + "} " + return_.getComment() + "\ndef " + function.getName() + "(");
    for (final Function.Parameter p : function.getParameters()) {
      result.append(p.getName()).append(", ");
    }
    // delete the last unnecessary " ,"
    result.delete(result.length() - 2, result.length());
    result.append(")\n    # Write your code here\nend");

    return result.toString();
  }
}
