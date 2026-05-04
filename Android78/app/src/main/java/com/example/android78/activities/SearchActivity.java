package com.example.android78.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android78.R;
import com.example.android78.adapters.PhotoGridAdapter;
import com.example.android78.models.Photo;
import com.example.android78.models.Tag;
import com.example.android78.utils.DataManager;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private DataManager dataManager;
    private AutoCompleteTextView searchInput;
    private RadioGroup typeGroup;
    private RadioGroup logicGroup;
    private List<Photo> searchResults;
    private PhotoGridAdapter resultAdapter;
    private List<Tag> selectedTags = new ArrayList<>();
    private ListView selectedTagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataManager = DataManager.getInstance();
        searchResults = new ArrayList<>();

        searchInput = findViewById(R.id.searchInput);
        typeGroup = findViewById(R.id.tagTypeGroup);
        logicGroup = findViewById(R.id.logicGroup);
        GridView resultsGrid = findViewById(R.id.searchResultsGrid);
        selectedTagsList = findViewById(R.id.selectedTagsList);
        Button searchBtn = findViewById(R.id.btnSearch);
        Button addTagBtn = findViewById(R.id.btnAddSearchTag);
        Button clearBtn = findViewById(R.id.btnClearTags);

        resultAdapter = new PhotoGridAdapter(this, searchResults);
        resultsGrid.setAdapter(resultAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAutocomplete(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        addTagBtn.setOnClickListener(v -> {
            String value = searchInput.getText().toString().trim();
            if (value.isEmpty()) return;
            Tag.TagType type = (typeGroup.getCheckedRadioButtonId() == R.id.radioPerson)
                    ? Tag.TagType.PERSON : Tag.TagType.LOCATION;
            Tag tag = new Tag(type, value);
            if (!selectedTags.contains(tag)) {
                selectedTags.add(tag);
                updateSelectedTagsDisplay();
                searchInput.setText("");
            }
        });

        clearBtn.setOnClickListener(v -> {
            selectedTags.clear();
            updateSelectedTagsDisplay();
        });

        searchBtn.setOnClickListener(v -> performSearch());
    }

    private void updateAutocomplete(String query) {
        if (query.isEmpty()) return;
        Tag.TagType type = (typeGroup.getCheckedRadioButtonId() == R.id.radioPerson)
                ? Tag.TagType.PERSON : Tag.TagType.LOCATION;
        List<String> suggestions = dataManager.getAutocompleteSuggestions(type, query);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        searchInput.setAdapter(adapter);
        searchInput.showDropDown();
    }

    private void updateSelectedTagsDisplay() {
        String[] tagStrings = selectedTags.stream().map(Tag::toString).toArray(String[]::new);
        selectedTagsList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, tagStrings));
    }

    private void performSearch() {
        List<Tag> tagsToSearch = new ArrayList<>(selectedTags);
        String currentInput = searchInput.getText().toString().trim();
        if (!currentInput.isEmpty()) {
            Tag.TagType type = (typeGroup.getCheckedRadioButtonId() == R.id.radioPerson)
                    ? Tag.TagType.PERSON : Tag.TagType.LOCATION;
            tagsToSearch.add(new Tag(type, currentInput));
        }
        if (tagsToSearch.isEmpty()) {
            Toast.makeText(this, "Enter at least one tag to search", Toast.LENGTH_SHORT).show();
            return;
        }
        searchResults.clear();
        boolean isConjunction = (logicGroup.getCheckedRadioButtonId() == R.id.radioAnd);
        List<Photo> found = isConjunction
                ? dataManager.searchConjunction(tagsToSearch)
                : dataManager.searchDisjunction(tagsToSearch);
        searchResults.addAll(found);
        resultAdapter.notifyDataSetChanged();
        Toast.makeText(this, found.size() + " photo(s) found", Toast.LENGTH_SHORT).show();
    }
}