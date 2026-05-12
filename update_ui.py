import re

file_path = r'mobile_app/src/main/java/com/agrotech/ai/ui/screens/CropRecommendationScreen.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace AgriTech to AgroTech
content = content.replace('AgriTech Solution AI', 'AgroTech Solution AI')

urls = {
    'rice': 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Paddy_field_in_Sri_Lanka.jpg/800px-Paddy_field_in_Sri_Lanka.jpg',
    'maize': 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Corn_Field_in_the_Evening.jpg/800px-Corn_Field_in_the_Evening.jpg',
    'chickpea': 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Chickpea.jpg/800px-Chickpea.jpg',
    'kidneybeans': 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Kidney_beans_in_a_bowl.jpg/800px-Kidney_beans_in_a_bowl.jpg',
    'pigeonpeas': 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Pigeon_peas_at_market.jpg/800px-Pigeon_peas_at_market.jpg',
    'mothbeans': 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Moth_bean_vigna_aconitifolia.jpg/800px-Moth_bean_vigna_aconitifolia.jpg',
    'mungbean': 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/38/Mung_beans.jpg/800px-Mung_beans.jpg',
    'blackgram': 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Vigna_mungo_seeds.jpg/800px-Vigna_mungo_seeds.jpg',
    'lentil': 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Lentils_in_a_bowl_2.jpg/800px-Lentils_in_a_bowl_2.jpg',
    'pomegranate': 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Pomegranates_%28Punica_granatum%29.jpg/800px-Pomegranates_%28Punica_granatum%29.jpg',
    'banana': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Bananas.jpg/800px-Bananas.jpg',
    'mango': 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Mango_on_tree_in_Kerala.jpg/800px-Mango_on_tree_in_Kerala.jpg',
    'grapes': 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Grapes.jpg/800px-Grapes.jpg',
    'watermelon': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Watermelons.jpg/800px-Watermelons.jpg',
    'muskmelon': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Cantaloupe.jpg/800px-Cantaloupe.jpg',
    'apple': 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/800px-Red_Apple.jpg',
    'orange': 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Orange-Fruit-Pieces.jpg/800px-Orange-Fruit-Pieces.jpg',
    'papaya': 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9f/Papaya_cross_section_BNC.jpg/800px-Papaya_cross_section_BNC.jpg',
    'coconut': 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Coconut.jpg/800px-Coconut.jpg',
    'cotton': 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Cotton_Plant.jpg/800px-Cotton_Plant.jpg',
    'jute': 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Corchorus_olitorius.jpg/800px-Corchorus_olitorius.jpg',
    'coffee': 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Roasted_coffee_beans.jpg/800px-Roasted_coffee_beans.jpg'
}

for crop, new_url in urls.items():
    content = re.sub(rf'\"{crop}\"\s*->\s*\".*?\"', f'\"{crop}\" -> \"{new_url}\"', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("UI Updated")
