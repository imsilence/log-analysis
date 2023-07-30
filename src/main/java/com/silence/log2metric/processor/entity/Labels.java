package com.silence.log2metric.processor.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NoArgsConstructor
public class Labels {
    @JsonAnyGetter
    @JsonAnySetter
    private final Map<String, String> labels = new HashMap<>();

    @JsonIgnore
    public Map<String, String> getLabels() {
        return labels;
    }

    public Labels(List<String> labelNames, Log value) {
        labelNames.forEach(new Consumer<String>() {
            @Override
            public void accept(String key) {
                labels.put(key, value.get(key));
            }
        });
    }
}
