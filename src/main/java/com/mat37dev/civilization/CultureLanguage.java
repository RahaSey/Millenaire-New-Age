package com.mat37dev.civilization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Random;

public record CultureLanguage(
        String id,
        List<String> maleFirstNames,
        List<String> femaleFirstNames,
        List<String> lastNames,
        List<String> villageNames
) {
    public static final Codec<CultureLanguage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(CultureLanguage::id),
                    Codec.STRING.listOf().fieldOf("male_first_names").forGetter(CultureLanguage::maleFirstNames),
                    Codec.STRING.listOf().fieldOf("female_first_names").forGetter(CultureLanguage::femaleFirstNames),
                    Codec.STRING.listOf().optionalFieldOf("last_names", List.of()).forGetter(CultureLanguage::lastNames),
                    Codec.STRING.listOf().fieldOf("village_names").forGetter(CultureLanguage::villageNames)
            ).apply(instance, CultureLanguage::new)
    );

    public String randomMaleName(Random random) {
        return maleFirstNames.get(random.nextInt(maleFirstNames.size()));
    }

    public String randomFemaleName(Random random) {
        return femaleFirstNames.get(random.nextInt(femaleFirstNames.size()));
    }

    public String randomLastName(Random random) {
        return lastNames.isEmpty() ? "" : lastNames.get(random.nextInt(lastNames.size()));
    }

    public String randomVillageName(Random random) {
        return villageNames.get(random.nextInt(villageNames.size()));
    }
}
