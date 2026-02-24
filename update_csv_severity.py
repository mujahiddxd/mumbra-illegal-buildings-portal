import csv
import random

# Read the current CSV file
input_file = 'src/main/resources/static/buildings.csv'
output_file = 'src/main/resources/static/buildings_updated.csv'

# Severity options
severity_options = ['HIGH', 'MEDIUM', 'LOW']

# Read and update the CSV
with open(input_file, 'r', encoding='utf-8') as infile:
    reader = csv.reader(infile)
    rows = list(reader)

# Update header to include Severity
if rows:
    header = rows[0]
    # Add Severity column after Longitude
    header.append('Severity')
    
    # Update data rows
    for i in range(1, len(rows)):
        if len(rows[i]) >= 10:  # Make sure row has enough columns
            # Add random severity based on floors
            floors = rows[i][4] if rows[i][4].isdigit() else '0'
            floors_num = int(floors)
            
            # Assign severity based on floors with some randomness
            if floors_num >= 8:
                severity = random.choice(['HIGH', 'HIGH', 'MEDIUM'])  # Mostly HIGH
            elif floors_num >= 5:
                severity = random.choice(['MEDIUM', 'MEDIUM', 'HIGH', 'LOW'])  # Mostly MEDIUM
            else:
                severity = random.choice(['LOW', 'LOW', 'MEDIUM'])  # Mostly LOW
            
            rows[i].append(severity)

# Write updated CSV
with open(output_file, 'w', newline='', encoding='utf-8') as outfile:
    writer = csv.writer(outfile)
    writer.writerows(rows)

print(f"Updated CSV saved to {output_file}")
print(f"Added severity column with random values based on building floors")