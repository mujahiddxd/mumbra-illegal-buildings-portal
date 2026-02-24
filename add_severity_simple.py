import random

# Read the current CSV file
with open('src/main/resources/static/buildings.csv', 'r', encoding='utf-8') as file:
    lines = file.readlines()

# Process the lines
updated_lines = []
for i, line in enumerate(lines):
    line = line.strip()
    if i == 0:  # Header line
        if 'Severity' not in line:
            line += ',Severity'
    else:  # Data lines
        if line and not line.endswith((',HIGH', ',MEDIUM', ',LOW')):
            # Extract floors (5th column)
            parts = line.split(',')
            if len(parts) >= 5:
                try:
                    floors = int(parts[4])
                except:
                    floors = 0
                
                # Assign severity based on floors
                if floors >= 8:
                    severity = random.choice(['HIGH', 'HIGH', 'MEDIUM'])
                elif floors >= 5:
                    severity = random.choice(['MEDIUM', 'MEDIUM', 'HIGH', 'LOW'])
                else:
                    severity = random.choice(['LOW', 'LOW', 'MEDIUM'])
                
                line += ',' + severity
    
    updated_lines.append(line)

# Write back to file
with open('src/main/resources/static/buildings.csv', 'w', encoding='utf-8') as file:
    for line in updated_lines:
        file.write(line + '\n')

print("Successfully added severity column to CSV file")