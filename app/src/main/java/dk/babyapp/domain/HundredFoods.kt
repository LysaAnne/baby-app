package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType

/** The common food allergens; worth giving early and often once the child eats solid food. */
enum class Allergen(val label: String) { Egg("Æg"), Milk("Mælk"), Gluten("Gluten"), Fish("Fisk"), Shellfish("Skaldyr"), Peanut("Jordnødder"), TreeNut("Nødder"), Sesame("Sesam"), Soy("Soja"), Celery("Selleri") }

enum class FoodGroup(val label: String) { Vegetables("Grøntsager"), Fruit("Frugt"), Grains("Korn"), Protein("Kød, fisk, æg og bælgfrugter"), Dairy("Mejeri"), NutsSeeds("Nødder, frø og fedtstoffer"), Herbs("Krydderier og urter") }

/**
 * One food on the 100 før 1 list. [stems] are matched against the start of the words in a registration's food
 * name, so "gulerødder" and "gulerodsmos" both count as Gulerod; a stem ending in a space must be the whole word. [tip] is a short serving note.
 */
data class FirstFood(val name: String, val group: FoodGroup, val allergen: Allergen? = null, val stems: List<String> = listOf(name.lowercase()), val tip: String? = null)

private fun food(name: String, group: FoodGroup, allergen: Allergen? = null, vararg stems: String, tip: String? = null) =
    FirstFood(name, group, allergen, stems.toList().ifEmpty { listOf(name.lowercase()) }, tip)

private const val HARD = "Hård: blend, riv eller hak den fint (Fødevarestyrelsen fraråder hele stykker før 3 år)."

val hundredFoods: List<FirstFood> = listOf(
    food("Gulerod", FoodGroup.Vegetables, null, "guler", tip = "Kogt blød; rå kun revet."),
    food("Kartoffel", FoodGroup.Vegetables, null, "kartof"),
    food("Sød kartoffel", FoodGroup.Vegetables, null, "sød kartof", "søde kartof", "batat"),
    food("Broccoli", FoodGroup.Vegetables),
    food("Blomkål", FoodGroup.Vegetables),
    food("Squash", FoodGroup.Vegetables, null, "squash", "courgette", "zucchini"),
    food("Ærter", FoodGroup.Vegetables, null, "ært"),
    food("Majs", FoodGroup.Vegetables, null, "majs"),
    food("Pastinak", FoodGroup.Vegetables),
    food("Persillerod", FoodGroup.Vegetables, null, "persillerod", "persillerød"),
    food("Hokkaido", FoodGroup.Vegetables, null, "hokkaido", "græskar "),
    food("Butternut", FoodGroup.Vegetables),
    food("Spinat", FoodGroup.Vegetables),
    food("Grønkål", FoodGroup.Vegetables),
    food("Rødbede", FoodGroup.Vegetables, null, "rødbede"),
    food("Knoldselleri", FoodGroup.Vegetables, Allergen.Celery, "knoldselleri", "selleri", tip = "Højt nitratindhold; alternativt majroe eller jordskok."),
    food("Porre", FoodGroup.Vegetables, null, "porre"),
    food("Løg", FoodGroup.Vegetables, null, "løg"),
    food("Tomat", FoodGroup.Vegetables),
    food("Agurk", FoodGroup.Vegetables, null, "agurk"),
    food("Peberfrugt", FoodGroup.Vegetables, null, "peberfrugt"),
    food("Avocado", FoodGroup.Vegetables),
    food("Aubergine", FoodGroup.Vegetables),
    food("Rosenkål", FoodGroup.Vegetables),
    food("Hvidkål", FoodGroup.Vegetables),
    food("Rødkål", FoodGroup.Vegetables),
    food("Grønne bønner", FoodGroup.Vegetables, null, "grønne bønne", "haricots"),
    food("Asparges", FoodGroup.Vegetables),
    food("Champignon", FoodGroup.Vegetables, null, "champignon", "svampe"),
    food("Kålrabi", FoodGroup.Vegetables),
    food("Pak choi", FoodGroup.Vegetables, null, "pak choi", "pak choy", "pakchoi"),
    food("Spidskål", FoodGroup.Vegetables),

    food("Æble", FoodGroup.Fruit, null, "æble"),
    food("Pære", FoodGroup.Fruit, null, "pære"),
    food("Banan", FoodGroup.Fruit),
    food("Blåbær", FoodGroup.Fruit),
    food("Jordbær", FoodGroup.Fruit),
    food("Hindbær", FoodGroup.Fruit),
    food("Brombær", FoodGroup.Fruit),
    food("Mango", FoodGroup.Fruit),
    food("Fersken", FoodGroup.Fruit),
    food("Blomme", FoodGroup.Fruit, null, "blomme"),
    food("Abrikos", FoodGroup.Fruit),
    food("Kirsebær", FoodGroup.Fruit, tip = "Uden sten, skåret i stykker."),
    food("Kiwi", FoodGroup.Fruit),
    food("Melon", FoodGroup.Fruit),
    food("Vandmelon", FoodGroup.Fruit),
    food("Appelsin", FoodGroup.Fruit),
    food("Mandarin", FoodGroup.Fruit),
    food("Vindruer", FoodGroup.Fruit, null, "vindrue", "druer", tip = "Skær dem på langs og fjern kernerne."),
    food("Ananas", FoodGroup.Fruit),
    food("Papaya", FoodGroup.Fruit),
    food("Nektarin", FoodGroup.Fruit),
    food("Citron og lime", FoodGroup.Fruit, null, "citron", "lime"),
    food("Passionsfrugt", FoodGroup.Fruit, null, "passion"),
    food("Figen", FoodGroup.Fruit, null, "figen", "fign"),
    food("Sveske", FoodGroup.Fruit, null, "svesk"),

    food("Havre", FoodGroup.Grains, Allergen.Gluten, "havre"),
    food("Hvede", FoodGroup.Grains, Allergen.Gluten, "hvede", "fuldkornshvede", "franskbrød", "hvedebrød"),
    food("Rug", FoodGroup.Grains, Allergen.Gluten, "rug"),
    food("Byg", FoodGroup.Grains, Allergen.Gluten, "byg", "perlebyg"),
    food("Spelt", FoodGroup.Grains, Allergen.Gluten),
    food("Couscous", FoodGroup.Grains, Allergen.Gluten),
    food("Bulgur", FoodGroup.Grains, Allergen.Gluten),
    food("Hirse", FoodGroup.Grains),
    food("Boghvede", FoodGroup.Grains),
    food("Quinoa", FoodGroup.Grains),
    food("Majsmel", FoodGroup.Grains, null, "majsmel", "polenta", "majsgrød"),
    food("Pasta", FoodGroup.Grains, Allergen.Gluten, "pasta", "spaghetti", "makaroni"),
    food("Fuldkornspasta", FoodGroup.Grains, Allergen.Gluten, "fuldkornspasta"),
    food("Pandekage", FoodGroup.Grains, null, "pandekage", tip = "Indeholder typisk æg, mælk og hvede."),
    food("Tortilla", FoodGroup.Grains, Allergen.Gluten, "tortilla"),
    food("Ris", FoodGroup.Grains, null, "ris", tip = "Ris indeholder arsen; Fødevarestyrelsen anbefaler at begrænse riskiks og risdrik."),

    food("Æg", FoodGroup.Protein, Allergen.Egg, "æg", tip = "Gennemkogt."),
    food("Kylling", FoodGroup.Protein),
    food("Kalkun", FoodGroup.Protein),
    food("Oksekød", FoodGroup.Protein, null, "okse"),
    food("Svinekød", FoodGroup.Protein, null, "svin", "gris"),
    food("Lam", FoodGroup.Protein, null, "lam"),
    food("Lever", FoodGroup.Protein, null, "lever "),
    food("Leverpostej", FoodGroup.Protein, null, "leverpostej"),
    food("Kalvekød", FoodGroup.Protein, null, "kalv"),
    food("Sild", FoodGroup.Protein, Allergen.Fish, "sild"),
    food("Sorte bønner", FoodGroup.Protein, null, "sorte bønne"),
    food("Smørbønner", FoodGroup.Protein, null, "smørbønne"),
    food("Laks", FoodGroup.Protein, Allergen.Fish),
    food("Torsk", FoodGroup.Protein, Allergen.Fish),
    food("Sej", FoodGroup.Protein, Allergen.Fish, "sej"),
    food("Makrel", FoodGroup.Protein, Allergen.Fish),
    food("Rejer", FoodGroup.Protein, Allergen.Shellfish, "reje"),
    food("Tofu", FoodGroup.Protein, Allergen.Soy),
    food("Edamame", FoodGroup.Protein, Allergen.Soy, "edamame", "sojabønne"),
    food("Røde linser", FoodGroup.Protein, null, "røde linse", "linse"),
    food("Kikærter", FoodGroup.Protein, null, "kikært", "hummus"),
    food("Kidneybønner", FoodGroup.Protein, null, "kidney"),
    food("Hvide bønner", FoodGroup.Protein, null, "hvide bønne"),

    food("Yoghurt", FoodGroup.Dairy, Allergen.Milk, "yoghurt"),
    food("Ost", FoodGroup.Dairy, Allergen.Milk, "ost"),
    food("Smør", FoodGroup.Dairy, Allergen.Milk, "smør "),
    food("Hytteost", FoodGroup.Dairy, Allergen.Milk),
    food("Komælk i maden", FoodGroup.Dairy, Allergen.Milk, "komælk", "mælk i maden", tip = "Højst 1 dl komælk om dagen i maden."),
    food("Creme fraiche", FoodGroup.Dairy, Allergen.Milk, "creme fraiche", "crème fraîche", "cremefraiche"),

    food("Jordnøddesmør", FoodGroup.NutsSeeds, Allergen.Peanut, "jordnød", "peanut", tip = HARD),
    food("Mandel", FoodGroup.NutsSeeds, Allergen.TreeNut, "mandel", tip = HARD),
    food("Cashew", FoodGroup.NutsSeeds, Allergen.TreeNut, "cashew", tip = HARD),
    food("Hasselnød", FoodGroup.NutsSeeds, Allergen.TreeNut, "hasselnød", tip = HARD),
    food("Valnød", FoodGroup.NutsSeeds, Allergen.TreeNut, "valnød", tip = HARD),
    food("Tahin", FoodGroup.NutsSeeds, Allergen.Sesame, "tahin", "sesam"),
    food("Chiafrø", FoodGroup.NutsSeeds, null, "chia"),
    food("Hørfrø", FoodGroup.NutsSeeds, null, "hørfrø", tip = "Hele frø anbefales ikke; brug hørfrøolie eller knuste frø."),
    food("Solsikkekerner", FoodGroup.NutsSeeds, null, "solsikke", tip = "Hele kerner anbefales ikke; brug solsikkeolie eller malede kerner."),
    food("Hampefrø", FoodGroup.NutsSeeds, null, "hamp", tip = "Hampefrøolie eller malede frø."),
    food("Græskarkerner", FoodGroup.NutsSeeds, null, "græskarkerne", tip = "Bør begrænses; malede."),
    food("Olivenolie", FoodGroup.NutsSeeds, null, "oliven", tip = "Oliven skåret i små stykker."),
    food("Kokos", FoodGroup.NutsSeeds, null, "kokos"),

    food("Persille", FoodGroup.Herbs, null, "persille"),
    food("Dild", FoodGroup.Herbs),
    food("Basilikum", FoodGroup.Herbs),
    food("Hvidløg", FoodGroup.Herbs, null, "hvidløg"),
    food("Oregano", FoodGroup.Herbs),
    food("Timian", FoodGroup.Herbs),
    food("Mynte", FoodGroup.Herbs),
    food("Koriander", FoodGroup.Herbs),
    food("Mild karry", FoodGroup.Herbs, null, "karry"),
    food("Kanel", FoodGroup.Herbs, tip = "Kun lidt: kanel indeholder kumarin."),
)

/** First and number of tastings of one food. */
data class FoodTasting(val first: Long, val count: Int)

/** Which foods on the list the child has had, found from the Fast føde registrations' food names. */
fun hundredFoodsProgress(events: List<CareEventEntity>, childId: String): Map<FirstFood, FoodTasting> {
    val meals = events.filter { it.childId == childId && it.type == CareEventType.SolidFood && it.deletedAt == null && !it.isDraft && it.foodName.isNotBlank() }
        .map { it.startedAt to " " + it.foodName.lowercase().replace(Regex("[^\\p{L}]+"), " ") + " " }
    return hundredFoods.mapNotNull { food ->
        val hits = meals.filter { (_, name) -> food.stems.any { stem -> name.contains(" $stem") } }
        if (hits.isEmpty()) null else food to FoodTasting(hits.minOf { it.first }, hits.size)
    }.toMap()
}
