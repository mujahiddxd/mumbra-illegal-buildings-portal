import random

# Read the CSV file
with open('src/main/resources/static/buildings.csv', 'r', encoding='utf-8') as file:
    lines = file.readlines()

# Process each line
updated_lines = []
for i, line in enumerate(lines):
    line = line.strip()
    if i == 0:  # Header line - keep as is
        updated_lines.append(line)
    else:  # Data lines
        if line and not line.endswith((',HIGH', ',MEDIUM', ',LOW')):
            # Split the line to get floors (5th column, index 4)
            parts = line.split(',')
            if len(parts) >= 5:
                try:
                    floors = int(parts[4])
                except:
                    floors = 0
                
                # Assign severity based on floors with randomness
                if floors >= 8:
                    severity = random.choice(['HIGH', 'HIGH', 'MEDIUM'])  # Mostly HIGH
                elif floors >= 5:
                    severity = random.choice(['MEDIUM', 'MEDIUM', 'HIGH', 'LOW'])  # Mostly MEDIUM
                else:
                    severity = random.choice(['LOW', 'LOW', 'MEDIUM'])  # Mostly LOW
                
                line += ',' + severity
        
        updated_lines.append(line)

# Write back to file
with open('src/main/resources/static/buildings.csv', 'w', encoding='utf-8') as file:
    for line in updated_lines:
        file.write(line + '\n')

print("Successfully added severity values to all building rows")