package com.example.project1311;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collection
{
    private Map<String, List<Question>> categories; // Categories for storing questions
    private String currentCategory; // Active category
    private int index; // Current question index within the active category

    public Collection() {
        categories = new HashMap<>();

        // Adding "Words" category
        List<Question> words = new ArrayList<>();
        words.add(new Question("Bonjour", "Hello"));
        words.add(new Question("Cest", "This"));
        words.add(new Question("Garcon", "Boy"));
        words.add(new Question("Merci", "Thank You"));
        words.add(new Question("Chat", "Cat"));
        words.add(new Question("Maison", "House"));
        words.add(new Question("Amis", "Friend"));
        words.add(new Question("Voiture", "Car"));
        words.add(new Question("École", "School"));
        words.add(new Question("Livre", "Book"));
        words.add(new Question("Pomme", "Apple"));
        words.add(new Question("Chien", "Dog"));
        words.add(new Question("Fille", "Girl"));
        words.add(new Question("Manger", "Eat"));
        words.add(new Question("Voix", "Voice"));
        words.add(new Question("Bureau", "Desk"));
        words.add(new Question("Chanson", "Song"));
        words.add(new Question("Soleil", "Sun"));
        words.add(new Question("Lune", "Moon"));
        words.add(new Question("Arbre", "Tree"));
        words.add(new Question("Roi", "King"));
        words.add(new Question("Mer", "Sea"));
        words.add(new Question("Plage", "Beach"));
        words.add(new Question("Chocolat", "Chocolate"));
        words.add(new Question("Nuit", "Night"));
        words.add(new Question("Ville", "City"));
        words.add(new Question("Forêt", "Forest"));
        words.add(new Question("Fleur", "Flower"));
        words.add(new Question("Papillon", "Butterfly"));
        words.add(new Question("Château", "Castle"));
        categories.put("Words", words);

        // Adding "Numbers" category
        List<Question> numbers = new ArrayList<>();
        numbers.add(new Question("Un", "One"));
        numbers.add(new Question("Deux", "Two"));
        numbers.add(new Question("Trois", "Three"));
        numbers.add(new Question("Quatre", "Four"));
        numbers.add(new Question("Cinq", "Five"));
        numbers.add(new Question("Six", "Six"));
        numbers.add(new Question("Sept", "Seven"));
        numbers.add(new Question("Huit", "Eight"));
        numbers.add(new Question("Neuf", "Nine"));
        numbers.add(new Question("Dix", "Ten"));
        categories.put("Numbers", numbers);

        // Adding "Family" category
        List<Question> family = new ArrayList<>();
        family.add(new Question("Mère", "Mother"));
        family.add(new Question("Père", "Father"));
        family.add(new Question("Frère", "Brother"));
        family.add(new Question("Sœur", "Sister"));
        family.add(new Question("Fils", "Son"));
        family.add(new Question("Fille", "Daughter"));
        family.add(new Question("Grand-mère", "Grandmother"));
        family.add(new Question("Grand-père", "Grandfather"));
        family.add(new Question("Oncle", "Uncle"));
        family.add(new Question("Tante", "Aunt"));
        categories.put("Family", family);

        currentCategory = "Words"; // Default category
        index = 0;
    }

    public void setCategory(String category) {
        if (categories.containsKey(category)) {
            currentCategory = category;
            index = 0; // Reset index when switching categories
        } else {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    public Question getNextQuestion() {
        List<Question> questions = categories.get(currentCategory);
        if (index < questions.size()) {
            Question q = questions.get(index);
            index++;
            return q;
        } else {
            throw new IndexOutOfBoundsException("No more questions in this category.");
        }
    }

    public boolean isNotLastQuestion() {
        List<Question> questions = categories.get(currentCategory);
        return index < questions.size();
    }

    public int getIndex() {
        return index;
    }

    public String getCurrentCategory() {
        return currentCategory;
    }

    public List<String> getAvailableCategories() {
        return new ArrayList<>(categories.keySet());
    }
}
