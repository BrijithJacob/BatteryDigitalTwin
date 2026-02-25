#!/usr/bin/env python3
"""
DTDL to Java Class Generator

This program converts Digital Twin Definition Language (DTDL) JSON files
into Java class definitions with proper annotations and types.
"""

import json
import os
from typing import Any, Dict, List


class DTDLJavaClassGenerator:
    """Generator for converting DTDL JSON to Java classes."""
    
    def __init__(self):
        self.type_mapping = {
            'integer': 'Integer',
            'double': 'Double',
            'string': 'String',
            'boolean': 'Boolean',
            'dateTime': 'LocalDateTime',
            'Array': 'List',
            'Object': 'Object'
        }
        
    def dtdl_to_java_type(self, schema: Any) -> str:
        """Convert DTDL schema to Java type."""
        if isinstance(schema, str):
            return self.type_mapping.get(schema, 'Object')
        
        if isinstance(schema, dict):
            schema_type = schema.get('@type', schema.get('type', 'Object'))
            
            if schema_type == 'Array':
                element_schema = schema.get('elementSchema', 'Object')
                element_type = self.dtdl_to_java_type(element_schema)
                return f'List<{element_type}>'
            
            elif schema_type == 'Object':
                return 'Map<String, Object>'
        
        return 'Object'
    
    def to_camel_case(self, snake_str: str) -> str:
        """Convert snake_case to camelCase."""
        components = snake_str.split('_')
        return components[0] + ''.join(x.title() for x in components[1:])
    
    def to_pascal_case(self, text: str) -> str:
        """Convert text to PascalCase for class names."""
        return ''.join(word.capitalize() for word in text.replace(' ', '_').split('_'))
    
    def generate_property_class(self, name: str, fields: List[Dict], package_name: str) -> str:
        """Generate a Java class for complex object properties."""
        class_name = self.to_pascal_case(name)
        
        lines = [
            f"package {package_name};",
            "",
            "import java.time.LocalDateTime;",
            "import java.util.List;",
            "import java.util.Map;",
            "",
            "/**",
            f" * {class_name} data class",
            " */",
            f"public class {class_name} {{",
            ""
        ]
        
        if not fields:
            lines.append("    // Empty class")
            lines.append("}")
            return '\n'.join(lines)
        
        # Add private fields
        for field in fields:
            field_name = field.get('name', 'unknown')
            field_schema = field.get('schema', 'Object')
            field_type = self.dtdl_to_java_type(field_schema)
            description = field.get('description', '')
            
            if description:
                lines.append(f"    /**")
                lines.append(f"     * {description}")
                lines.append(f"     */")
            lines.append(f"    private {field_type} {self.to_camel_case(field_name)};")
            lines.append("")
        
        # Add default constructor
        lines.append(f"    public {class_name}() {{}}")
        lines.append("")
        
        # Add parameterized constructor
        params = []
        for field in fields:
            field_name = field.get('name', 'unknown')
            field_schema = field.get('schema', 'Object')
            field_type = self.dtdl_to_java_type(field_schema)
            params.append(f"{field_type} {self.to_camel_case(field_name)}")
        
        lines.append(f"    public {class_name}({', '.join(params)}) {{")
        for field in fields:
            field_name = self.to_camel_case(field.get('name', 'unknown'))
            lines.append(f"        this.{field_name} = {field_name};")
        lines.append("    }")
        lines.append("")
        
        # Add getters and setters
        for field in fields:
            field_name = field.get('name', 'unknown')
            camel_name = self.to_camel_case(field_name)
            pascal_name = self.to_pascal_case(field_name)
            field_schema = field.get('schema', 'Object')
            field_type = self.dtdl_to_java_type(field_schema)
            
            # Getter
            lines.append(f"    public {field_type} get{pascal_name}() {{")
            lines.append(f"        return {camel_name};")
            lines.append("    }")
            lines.append("")
            
            # Setter
            lines.append(f"    public void set{pascal_name}({field_type} {camel_name}) {{")
            lines.append(f"        this.{camel_name} = {camel_name};")
            lines.append("    }")
            lines.append("")
        
        lines.append("}")
        
        return '\n'.join(lines)
    
    def generate_nested_classes(self, schema: Dict, base_name: str, package_name: str) -> List[tuple]:
        """Generate nested classes for complex schemas. Returns list of (filename, content) tuples."""
        classes = []
        
        if isinstance(schema, dict):
            schema_type = schema.get('@type', schema.get('type'))
            
            if schema_type == 'Object':
                fields = schema.get('fields', [])
                
                # Generate classes for nested objects
                for field in fields:
                    field_schema = field.get('schema')
                    if isinstance(field_schema, dict):
                        field_type = field_schema.get('@type', field_schema.get('type'))
                        if field_type == 'Object':
                            nested_name = f"{base_name}_{field['name']}"
                            nested_class = self.generate_property_class(nested_name, field_schema.get('fields', []), package_name)
                            class_name = self.to_pascal_case(nested_name)
                            classes.append((f"{class_name}.java", nested_class))
                            
                            # Recursively generate deeper nested classes
                            deeper_classes = self.generate_nested_classes(field_schema, nested_name, package_name)
                            classes.extend(deeper_classes)
                
                # Generate the main class
                main_class = self.generate_property_class(base_name, fields, package_name)
                class_name = self.to_pascal_case(base_name)
                classes.append((f"{class_name}.java", main_class))
        
        return classes
    
    def generate_interface_class(self, dtdl_data: Dict, package_name: str) -> tuple:
        """Generate the main interface class from DTDL data. Returns (filename, content, nested_classes)."""
        interface_id = dtdl_data.get('@id', 'Unknown')
        display_name = dtdl_data.get('displayName', 'UnknownInterface')
        description = dtdl_data.get('description', '')
        contents = dtdl_data.get('contents', [])
        
        # Create a valid Java class name
        class_name = self.to_pascal_case(display_name)
        
        lines = [
            f"package {package_name};",
            "",
            "import java.time.LocalDateTime;",
            "import java.util.List;",
            "import java.util.Map;",
            "import java.util.HashMap;",
            "",
            "/**",
            f" * {display_name}",
            " *",
            f" * DTDL ID: {interface_id}",
            f" * Description: {description}",
            " */",
            f"public class {class_name} {{",
            ""
        ]
        
        # Generate nested classes
        nested_classes = []
        for content in contents:
            content_type = content.get('@type')
            content_name = content.get('name', 'unknown')
            content_schema = content.get('schema')
            
            if content_type in ['Property', 'Telemetry']:
                if isinstance(content_schema, dict):
                    nested = self.generate_nested_classes(content_schema, content_name, package_name)
                    nested_classes.extend(nested)
            
            elif content_type == 'Command':
                # Generate classes for request/response schemas
                request_schema = content.get('request', {}).get('schema')
                response_schema = content.get('response', {}).get('schema')
                
                if isinstance(request_schema, dict):
                    request_classes = self.generate_nested_classes(request_schema, f"{content_name}_request", package_name)
                    nested_classes.extend(request_classes)
                
                if isinstance(response_schema, dict):
                    response_classes = self.generate_nested_classes(response_schema, f"{content_name}_response", package_name)
                    nested_classes.extend(response_classes)
        
        # Add properties as fields
        properties = []
        for content in contents:
            content_type = content.get('@type')
            content_name = content.get('name', 'unknown')
            content_description = content.get('description', '')
            
            if content_type == 'Property':
                schema = content.get('schema')
                java_type = self.dtdl_to_java_type(schema)
                properties.append((content_name, java_type, content_description))
            
            elif content_type == 'Component':
                schema = content.get('schema', '')
                component_type = schema.split(';')[0].split(':')[-1] if ':' in schema else 'Object'
                properties.append((content_name, self.to_pascal_case(component_type), content.get('displayName', '')))
        
        # Add private fields
        for prop_name, prop_type, prop_desc in properties:
            if prop_desc:
                lines.append(f"    /**")
                lines.append(f"     * {prop_desc}")
                lines.append(f"     */")
            lines.append(f"    private {prop_type} {self.to_camel_case(prop_name)};")
            lines.append("")
        
        # Add constructor
        lines.append(f"    public {class_name}() {{}}")
        lines.append("")
        
        # Add getters and setters
        for prop_name, prop_type, prop_desc in properties:
            camel_name = self.to_camel_case(prop_name)
            pascal_name = self.to_pascal_case(prop_name)
            
            # Getter
            lines.append(f"    public {prop_type} get{pascal_name}() {{")
            lines.append(f"        return {camel_name};")
            lines.append("    }")
            lines.append("")
            
            # Setter
            lines.append(f"    public void set{pascal_name}({prop_type} {camel_name}) {{")
            lines.append(f"        this.{camel_name} = {camel_name};")
            lines.append("    }")
            lines.append("")
        
        # Add methods for telemetry
        telemetry = [c for c in contents if c.get('@type') == 'Telemetry']
        if telemetry:
            lines.append("    // Telemetry methods")
            for tel in telemetry:
                tel_name = tel.get('name', 'unknown')
                lines.append(f"    public void send{self.to_pascal_case(tel_name)}(Map<String, Object> data) {{")
                lines.append(f"        // TODO: Implement {tel_name} telemetry")
                lines.append("    }")
                lines.append("")
        
        # Add methods for commands
        commands = [c for c in contents if c.get('@type') == 'Command']
        if commands:
            lines.append("    // Command methods")
            for cmd in commands:
                cmd_name = cmd.get('name', 'unknown')
                lines.append(f"    public Map<String, Object> {self.to_camel_case(cmd_name)}(Map<String, Object> params) {{")
                lines.append(f"        // TODO: Implement {cmd_name} command")
                lines.append("        return new HashMap<>();")
                lines.append("    }")
                lines.append("")
        
        lines.append("}")
        
        return (f"{class_name}.java", '\n'.join(lines), nested_classes)
    
    def generate_from_file(self, input_file: str, output_dir: str, package_name: str) -> None:
        """Generate Java class files from DTDL JSON file."""
        print(f"Processing {input_file}...")
        
        with open(input_file, 'r') as f:
            dtdl_data = json.load(f)
        
        filename, class_code, nested_classes = self.generate_interface_class(dtdl_data, package_name)
        
        # Create output directory if it doesn't exist
        os.makedirs(output_dir, exist_ok=True)
        
        # Write main class
        main_path = os.path.join(output_dir, filename)
        with open(main_path, 'w') as f:
            f.write(class_code)
        print(f"  Generated {filename}")
        
        # Write nested classes
        for nested_filename, nested_code in nested_classes:
            nested_path = os.path.join(output_dir, nested_filename)
            with open(nested_path, 'w') as f:
                f.write(nested_code)
            print(f"  Generated {nested_filename}")


def main():
    """Main function to generate Java classes from DTDL files."""
    generator = DTDLJavaClassGenerator()
    
    # Define input files and output directories
    files = [
        ('NNBatterySystemDTDL.json', 'physical_system', 'com.battery.physicalsystem'),
        ('NNBatteryLearning Model.json', 'neural_network', 'com.battery.neuralnetwork'),
        ('NNBatteryMainDTDL.json', 'digital_twin', 'com.battery.digitaltwin')
    ]
    
    # Generate classes for each DTDL file
    for input_file, output_subdir, package_name in files:
        input_path = f'/home/cusat/bree/validateddtdls/{input_file}'
        output_dir = f'/home/cusat/bree/validateddtdls/{output_subdir}'
        
        if os.path.exists(input_path):
            generator.generate_from_file(input_path, output_dir, package_name)
        else:
            print(f"Warning: {input_path} not found")
    
    print("\nJava class generation complete!")


if __name__ == '__main__':
    main()
