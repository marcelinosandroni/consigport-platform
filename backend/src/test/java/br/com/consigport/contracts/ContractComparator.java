package br.com.consigport.contracts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The comparison of the contract published by the application with the snapshot.
 *
 * <p>The snapshot {@code contracts/consigport-api.yaml} is the source of truth of the
 * public API. This class reads both documents and answers the differences that matter
 * to a consumer of the API:
 *
 * <ul>
 *   <li>the routes and the methods of the routes;
 *   <li>the {@code operationId}, the name the generated client calls;
 *   <li>the parameters of every operation, with their name, their place and whether they
 *       are required;
 *   <li>the status codes of every answer;
 *   <li>the shape of the bodies: the names of the fields, their type, their enum and
 *       the fields that are always present.
 * </ul>
 *
 * <p>The comparison ignores what does not change the contract of the consumer: the
 * descriptions, the examples, the formats, the numeric bounds and the name of the
 * components. The bounds of the request stay in the snapshot and the server keeps
 * enforcing them with the validation of the edge.
 */
final class ContractComparator {

  private final Map<String, Object> contract;
  private final Map<String, Object> generated;
  private final List<String> differences = new ArrayList<>();

  private ContractComparator(Map<String, Object> contract, Map<String, Object> generated) {
    this.contract = contract;
    this.generated = generated;
  }

  /**
   * Compares the two documents.
   *
   * @param contract the snapshot of the public API
   * @param generated the document published by the application
   * @return the differences, empty when the documents agree
   */
  static List<String> compare(Map<String, Object> contract, Map<String, Object> generated) {
    ContractComparator comparator = new ContractComparator(contract, generated);
    comparator.comparePaths(map(contract.get("paths")), map(generated.get("paths")));
    return comparator.differences;
  }

  private void comparePaths(Map<String, Object> contractPaths, Map<String, Object> generatedPaths) {
    for (Map.Entry<String, Object> entry : contractPaths.entrySet()) {
      String path = entry.getKey();
      Map<String, Object> contractPath = map(entry.getValue());
      Map<String, Object> generatedPath = map(generatedPaths.get(path));

      if (generatedPath.isEmpty()) {
        differences.add("the route " + path + " is missing in the generated document");
        continue;
      }

      for (Map.Entry<String, Object> operation : contractPath.entrySet()) {
        String method = operation.getKey();
        Map<String, Object> generatedOperation = map(generatedPath.get(method));

        if (generatedOperation.isEmpty()) {
          differences.add("the operation " + method.toUpperCase() + " " + path + " is missing");
          continue;
        }
        compareOperation(
            method.toUpperCase() + " " + path, map(operation.getValue()), generatedOperation);
      }

      for (String method : generatedPath.keySet()) {
        if (!contractPath.containsKey(method)) {
          differences.add(
              "the generated document declares "
                  + method.toUpperCase()
                  + " "
                  + path
                  + ", and the contract does not");
        }
      }
    }

    for (String path : generatedPaths.keySet()) {
      if (!contractPaths.containsKey(path)) {
        differences.add(
            "the generated document declares the route " + path + ", and the contract does not");
      }
    }
  }

  private void compareOperation(
      String label, Map<String, Object> contractOperation, Map<String, Object> generatedOperation) {
    String contractId = text(contractOperation.get("operationId"));
    String generatedId = text(generatedOperation.get("operationId"));

    if (!Objects.equals(contractId, generatedId)) {
      differences.add(
          label
              + ": the operationId is "
              + generatedId
              + " and the contract declares "
              + contractId);
    }

    compareParameters(
        label,
        list(contractOperation.get("parameters")),
        list(generatedOperation.get("parameters")));
    compareRequestBody(
        label, contractOperation.get("requestBody"), generatedOperation.get("requestBody"));
    compareResponses(
        label,
        map(contractOperation.get("responses")),
        map(generatedOperation.get("responses")));
  }

  private void compareParameters(String label, List<Object> contractParameters, List<Object> generatedParameters) {
    Set<String> expected = canonicalParameters(contractParameters, contract);
    Set<String> actual = canonicalParameters(generatedParameters, generated);

    for (String parameter : expected) {
      if (!actual.contains(parameter)) {
        differences.add(label + ": the parameter " + parameter + " is missing");
      }
    }
    for (String parameter : actual) {
      if (!expected.contains(parameter)) {
        differences.add(label + ": the parameter " + parameter + " is not in the contract");
      }
    }
  }

  private Set<String> canonicalParameters(List<Object> parameters, Map<String, Object> document) {
    Set<String> canonical = new TreeSet<>();

    for (Object parameter : parameters) {
      Map<String, Object> resolved = resolve(map(parameter), document);
      canonical.add(
          text(resolved.get("name"))
              + " ("
              + text(resolved.get("in"))
              + ", required: "
              + bool(resolved.get("required"))
              + ")");
    }
    return canonical;
  }

  private void compareRequestBody(String label, Object contractBody, Object generatedBody) {
    if (contractBody == null) {
      if (generatedBody != null) {
        differences.add(label + ": the generated document declares a request body");
      }
      return;
    }
    if (generatedBody == null) {
      differences.add(label + ": the request body is missing");
      return;
    }

    Map<String, Object> contractRequest = resolve(map(contractBody), contract);
    Map<String, Object> generatedRequest = map(generatedBody);

    if (!Objects.equals(
        bool(contractRequest.get("required")), bool(generatedRequest.get("required")))) {
      differences.add(
          label + ": the request body is required: " + bool(generatedRequest.get("required")));
    }
    compareSchemas(
        label + " request body",
        schemaOf(contractRequest),
        schemaOf(generatedRequest),
        contract,
        generated);
  }

  private void compareResponses(
      String label, Map<String, Object> contractResponses, Map<String, Object> generatedResponses) {
    for (String status : contractResponses.keySet()) {
      Map<String, Object> generatedResponse = map(generatedResponses.get(status));

      if (generatedResponse.isEmpty()) {
        differences.add(label + ": the answer " + status + " is missing");
        continue;
      }
      compareSchemas(
          label + " answer " + status,
          schemaOf(resolve(map(contractResponses.get(status)), contract)),
          schemaOf(generatedResponse),
          contract,
          generated);
    }

    for (String status : generatedResponses.keySet()) {
      if (!contractResponses.containsKey(status)) {
        differences.add(
            label + ": the generated document answers " + status + ", and the contract does not");
      }
    }
  }

  private void compareSchemas(
      String label,
      Object contractSchema,
      Object generatedSchema,
      Map<String, Object> contractDocument,
      Map<String, Object> generatedDocument) {
    if (contractSchema == null) {
      return;
    }
    if (generatedSchema == null) {
      differences.add(label + ": the schema of the body is missing");
      return;
    }

    Map<String, Object> expected = canonicalSchema(contractSchema, contractDocument);
    Map<String, Object> actual = canonicalSchema(generatedSchema, generatedDocument);

    if (!Objects.equals(expected.get("type"), actual.get("type"))) {
      differences.add(
          label
              + ": the type is "
              + actual.get("type")
              + " and the contract declares "
              + expected.get("type"));
    }

    Set<String> expectedRequired = (Set<String>) expected.get("required");
    Set<String> actualRequired = (Set<String>) actual.get("required");
    if (!expectedRequired.equals(actualRequired)) {
      differences.add(
          label
              + ": the fields always present are "
              + actualRequired
              + " and the contract declares "
              + expectedRequired);
    }

    Map<String, Object> expectedProperties = (Map<String, Object>) expected.get("properties");
    Map<String, Object> actualProperties = (Map<String, Object>) actual.get("properties");

    for (Map.Entry<String, Object> entry : expectedProperties.entrySet()) {
      Object actualProperty = actualProperties.get(entry.getKey());
      if (actualProperty == null) {
        differences.add(label + ": the field " + entry.getKey() + " is missing");
        continue;
      }
      if (!Objects.equals(entry.getValue(), actualProperty)) {
        differences.add(
            label
                + ": the field "
                + entry.getKey()
                + " is "
                + actualProperty
                + " and the contract declares "
                + entry.getValue());
      }
    }

    for (String property : actualProperties.keySet()) {
      if (!expectedProperties.containsKey(property)) {
        differences.add(label + ": the field " + property + " is not in the contract");
      }
    }
  }

  private Map<String, Object> canonicalSchema(Object raw, Map<String, Object> document) {
    Map<String, Object> schema = flatten(resolve(map(raw), document), document);
    Map<String, Object> canonical = new LinkedHashMap<>();

    canonical.put("type", canonicalType(schema, document));
    canonical.put("required", new TreeSet<>(texts(schema.get("required"))));
    canonical.put("properties", canonicalProperties(schema, document));
    return canonical;
  }

  private Map<String, Object> canonicalProperties(
      Map<String, Object> schema, Map<String, Object> document) {
    Map<String, Object> properties = new TreeMap<>();

    for (Map.Entry<String, Object> entry : map(schema.get("properties")).entrySet()) {
      properties.put(entry.getKey(), canonicalProperty(entry.getValue(), document));
    }
    return properties;
  }

  private Map<String, Object> canonicalProperty(Object raw, Map<String, Object> document) {
    Map<String, Object> schema = resolve(map(raw), document);

    if (schema.containsKey("oneOf")) {
      for (Object branch : list(schema.get("oneOf"))) {
        Map<String, Object> resolved = flatten(resolve(map(branch), document), document);
        if (!"null".equals(canonicalType(resolved, document))) {
          return canonicalProperty(resolved, document);
        }
      }
    }

    Map<String, Object> property = new LinkedHashMap<>();
    property.put("type", canonicalType(schema, document));
    List<String> values = texts(schema.get("enum"));
    if (!values.isEmpty()) {
      property.put("enum", new TreeSet<>(values));
    }
    return property;
  }

  private String canonicalType(Map<String, Object> schema, Map<String, Object> document) {
    Object type = schema.get("type");

    if (type instanceof String name) {
      return name;
    }
    if (type instanceof List<?> names) {
      for (Object name : names) {
        if (!"null".equals(name)) {
          return String.valueOf(name);
        }
      }
      return "null";
    }
    if (schema.containsKey("oneOf")) {
      for (Object branch : list(schema.get("oneOf"))) {
        String branchType = canonicalType(flatten(resolve(map(branch), document), document), document);
        if (!"null".equals(branchType)) {
          return branchType;
        }
      }
    }
    if (schema.containsKey("properties")) {
      return "object";
    }
    if (schema.containsKey("items")) {
      return "array";
    }
    return "unknown";
  }

  /** Follows the reference of a schema or of a component. */
  private Map<String, Object> resolve(Map<String, Object> node, Map<String, Object> document) {
    Object reference = node.get("$ref");
    if (!(reference instanceof String text)) {
      return node;
    }
    String[] path = text.replace("#/", "").split("/");
    Object current = document;
    for (String step : path) {
      if (!(current instanceof Map<?, ?> map)) {
        return node;
      }
      current = map.get(step);
    }
    return map(current);
  }

  /** Merges the branches of an {@code allOf} into one schema. */
  private Map<String, Object> flatten(Map<String, Object> schema, Map<String, Object> document) {
    if (!schema.containsKey("allOf")) {
      return schema;
    }

    Map<String, Object> properties = new TreeMap<>();
    Set<String> required = new TreeSet<>();

    for (Object branch : list(schema.get("allOf"))) {
      Map<String, Object> resolved = flatten(resolve(map(branch), document), document);
      properties.putAll(map(resolved.get("properties")));
      required.addAll(texts(resolved.get("required")));
    }

    Map<String, Object> merged = new LinkedHashMap<>();
    merged.put("type", "object");
    merged.put("properties", properties);
    merged.put("required", new ArrayList<>(required));
    return merged;
  }

  /** The schema of the first media type of a body. */
  private Object schemaOf(Map<String, Object> body) {
    Map<String, Object> content = map(body.get("content"));
    for (Object media : content.values()) {
      return map(media).get("schema");
    }
    return null;
  }

  private static Map<String, Object> map(Object raw) {
    if (raw instanceof Map<?, ?> source) {
      Map<String, Object> target = new LinkedHashMap<>();
      source.forEach((key, value) -> target.put(String.valueOf(key), value));
      return target;
    }
    return new LinkedHashMap<>();
  }

  private static List<Object> list(Object raw) {
    if (raw instanceof List<?> source) {
      return new ArrayList<>(source);
    }
    return new ArrayList<>();
  }

  private static String text(Object raw) {
    return raw == null ? "" : String.valueOf(raw);
  }

  private static boolean bool(Object raw) {
    return raw instanceof Boolean value && value;
  }

  private static List<String> texts(Object raw) {
    List<String> values = new ArrayList<>();
    for (Object item : list(raw)) {
      values.add(String.valueOf(item));
    }
    return values;
  }
}
