import csv
import random

# Read the original CSV file
input_file = 'src/main/resources/static/buildings.csv'

# Severity options
severity_options = ['HIGH', 'MEDIUM', 'LOW']

# Read the CSV
with open(input_file, 'r', encoding='utf-8') as infile:
    content = infile.read()

# Split into lines
lines = content.strip().split('\n')

# Process header
header_line = lines[0]
if 'Severity' not in header_line:
    header_line += ',Severity'

# Process data lines
updated_lines = [header_line]

for i in range(1, len(lines)):
    line = lines[i].strip()
    if line and not line.endswith(',HIGH') and not line.endswith(',MEDIUM') and not line.endswith(',LOW'):
        # Extract floors from the line (5th column, index 4)
        parts = line.split(',')
        if len(parts) >= 5:
            floors_str = parts[4]
            try:
                floors_num = int(floors_str)
            except:
                floors_num = 0
            
            # Assign severity based on floors with some randomness
            if floors_num >= 8:
                severity = random.choice(['HIGH', 'HIGH', 'MEDIUM'])  # Mostly HIGH
            elif floors_num >= 5:
                severity = random.choice(['MEDIUM', 'MEDIUM', 'HIGH', 'LOW'])  # Mostly MEDIUM
            else:
                severity = random.choice(['LOW', 'LOW', 'MEDIUM'])  # Mostly LOW
            
            line += ',' + severity
        
        updated_lines.append(line)
    elif line:
        updated_lines.append(line)

# Write back to the original file
with open(input_file, 'w', encoding='utf-8') as outfile:
    outfile.write('\n'.join(updated_lines))

print(f"Updated {input_file} with severity column")
print(f"Added severity values: {severity_options}")