#!/usr/bin/env python3
"""
DTDL to Python Class Generator

This program converts Digital Twin Definition Language (DTDL) JSON files
into Python class definitions with proper type hints and data models.
"""

import json
import os
from typing import Any, Dict, List, Optional
from dataclasses import dataclass
from datetime import datetime


class DTDLClassGenerator:
    """Generator for converting DTDL JSON to Python classes."""
    
    def __init__(self):
        self.type_mapping = {
            'integer': 'int',
            'double': 'float',
            'string': 'str',
            'boolean': 'bool',
            'dateTime': 'datetime',
            'Array': 'List',
            'Object': 'object'
        }
        
    def dtdl_to_python_type(self, schema: Any, is_array: bool = False) -> str:
        """Convert DTDL schema to Python type hint."""
        if isinstance(schema, str):
            return self.type_mapping.get(schema, 'Any')
        
        if isinstance(schema, dict):
            schema_type = schema.get('@type', schema.get('type', 'Any'))
            
            if schema_type == 'Array':
                element_schema = schema.get('elementSchema', 'Any')
                element_type = self.dtdl_to_python_type(element_schema)
                return f'List[{element_type}]'
            
            elif schema_type == 'Object':
                # Return a placeholder class name that will be generated
                return 'Dict[str, Any]'  # Can be customized to create nested classes
        
        return 'Any'
    
    def generate_property_class(self, name: str, fields: List[Dict]) -> str:
        """Generate a class for complex object properties."""
        class_name = ''.join(word.capitalize() for word in name.split('_'))
        
        lines = [
            f"@dataclass",
            f"class {class_name}:"
        ]
        
        if not fields:
            lines.append("    pass")
            return '\n'.join(lines)
        
        for field in fields:
            field_name = field.get('name', 'unknown')
            field_schema = field.get('schema', 'Any')
            field_type = self.dtdl_to_python_type(field_schema)
            description = field.get('description', '')
            
            if description:
                lines.append(f"    # {description}")
            lines.append(f"    {field_name}: {field_type}")
        
        return '\n'.join(lines)
    
    def generate_nested_classes(self, schema: Dict, base_name: str) -> List[str]:
        """Generate nested classes for complex schemas."""
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
                            nested_class = self.generate_property_class(nested_name, field_schema.get('fields', []))
                            classes.append(nested_class)
                            
                            # Recursively generate deeper nested classes
                            deeper_classes = self.generate_nested_classes(field_schema, nested_name)
                            classes.extend(deeper_classes)
                
                # Generate the main class
                main_class = self.generate_property_class(base_name, fields)
                classes.append(main_class)
        
        return classes
    
    def generate_interface_class(self, dtdl_data: Dict) -> str:
        """Generate the main interface class from DTDL data."""
        interface_id = dtdl_data.get('@id', 'Unknown')
        display_name = dtdl_data.get('displayName', 'UnknownInterface')
        description = dtdl_data.get('description', '')
        contents = dtdl_data.get('contents', [])
        
        # Create a valid Python class name
        class_name = ''.join(word.capitalize() for word in display_name.replace(' ', '_').split('_'))
        
        lines = [
            '"""',
            f'{display_name}',
            '',
            f'DTDL ID: {interface_id}',
            f'Description: {description}',
            '"""',
            '',
            'from dataclasses import dataclass, field',
            'from typing import List, Dict, Optional, Any',
            'from datetime import datetime',
            '',
            ''
        ]
        
        # Generate nested classes first
        nested_classes = []
        for content in contents:
            content_type = content.get('@type')
            content_name = content.get('name', 'unknown')
            content_schema = content.get('schema')
            
            if content_type in ['Property', 'Telemetry']:
                if isinstance(content_schema, dict):
                    nested = self.generate_nested_classes(content_schema, content_name)
                    nested_classes.extend(nested)
            
            elif content_type == 'Command':
                # Generate classes for request/response schemas
                request_schema = content.get('request', {}).get('schema')
                response_schema = content.get('response', {}).get('schema')
                
                if isinstance(request_schema, dict):
                    request_classes = self.generate_nested_classes(request_schema, f"{content_name}_request")
                    nested_classes.extend(request_classes)
                
                if isinstance(response_schema, dict):
                    response_classes = self.generate_nested_classes(response_schema, f"{content_name}_response")
                    nested_classes.extend(response_classes)
        
        # Add nested classes
        for nested_class in nested_classes:
            lines.append(nested_class)
            lines.append('')
            lines.append('')
        
        # Generate main interface class
        lines.append('@dataclass')
        lines.append(f'class {class_name}:')
        lines.append(f'    """')
        lines.append(f'    {description}')
        lines.append(f'    """')
        
        # Add properties
        properties = []
        telemetry = []
        commands = []
        
        for content in contents:
            content_type = content.get('@type')
            content_name = content.get('name', 'unknown')
            content_description = content.get('description', '')
            
            if content_type == 'Property':
                schema = content.get('schema')
                python_type = self.dtdl_to_python_type(schema)
                properties.append((content_name, python_type, content_description))
            
            elif content_type == 'Component':
                schema = content.get('schema', '')
                component_type = schema.split(';')[0].split(':')[-1] if ':' in schema else 'Any'
                properties.append((content_name, component_type, content.get('displayName', '')))
            
            elif content_type == 'Telemetry':
                telemetry.append(content_name)
            
            elif content_type == 'Command':
                commands.append(content_name)
        
        if not properties and not telemetry and not commands:
            lines.append('    pass')
        else:
            # Add properties as class fields
            for prop_name, prop_type, prop_desc in properties:
                if prop_desc:
                    lines.append(f'    # {prop_desc}')
                lines.append(f'    {prop_name}: Optional[{prop_type}] = None')
        
        # Add methods for telemetry
        if telemetry:
            lines.append('')
            lines.append('    # Telemetry methods')
            for tel_name in telemetry:
                lines.append(f'    def send_{tel_name}(self, data: Dict[str, Any]) -> None:')
                lines.append(f'        """Send {tel_name} telemetry data."""')
                lines.append(f'        pass  # Implementation required')
                lines.append('')
        
        # Add methods for commands
        if commands:
            lines.append('    # Command methods')
            for cmd_name in commands:
                lines.append(f'    def {cmd_name}(self, **kwargs) -> Dict[str, Any]:')
                lines.append(f'        """Execute {cmd_name} command."""')
                lines.append(f'        pass  # Implementation required')
                lines.append('')
        
        return '\n'.join(lines)
    
    def generate_from_file(self, input_file: str, output_file: str) -> None:
        """Generate Python class file from DTDL JSON file."""
        print(f"Processing {input_file}...")
        
        with open(input_file, 'r') as f:
            dtdl_data = json.load(f)
        
        class_code = self.generate_interface_class(dtdl_data)
        
        with open(output_file, 'w') as f:
            f.write(class_code)
        
        print(f"Generated {output_file}")


def main():
    """Main function to generate classes from DTDL files."""
    generator = DTDLClassGenerator()
    
    # Define input and output files
    files = [
        ('NNBatterySystemDTDL.json', 'physical_battery_system.py'),
        ('NNBatteryLearning Model.json', 'neural_network_model.py'),
        ('NNBatteryMainDTDL.json', 'battery_digital_twin.py')
    ]
    
    # Generate classes for each DTDL file
    for input_file, output_file in files:
        input_path = f'/home/cusat/bree/validateddtdls/{input_file}'
        output_path = f'/home/cusat/bree/validateddtdls/{output_file}'
        
        if os.path.exists(input_path):
            generator.generate_from_file(input_path, output_path)
        else:
            print(f"Warning: {input_path} not found")
    
    print("\nClass generation complete!")


if __name__ == '__main__':
    main()
