import random

# Read the CSV file
with open('src/main/resources/static/buildings.csv', 'r', encoding='utf-8') as file:
    content = file.read()

lines = content.strip().split('\n')
updated_lines = []

for i, line in enumerate(lines):
    if i == 0:  # Header
        updated_lines.append(line)
    else:
        # Check if line already has severity
        if line.endswith((',HIGH', ',MEDIUM', ',LOW')):
            updated_lines.append(line)
        else:
            # Add severity based on floors
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
    file.write('\n'.join(updated_lines))

print("Successfully updated all rows with severity values")