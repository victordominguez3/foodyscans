package com.example.foodyscans.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodyscans.mappers.toMap
import com.example.foodyscans.mappers.toProduct
import com.example.foodyscans.mappers.toRecipe
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class UserViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    init {
        fetchUserData()
    }

    private fun fetchUserData() {

        var user = User()

        if (auth.currentUser != null) {
            db.collection("users").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.id == auth.currentUser!!.uid) {
                            user = User(
                                name = document.getString("name") ?: "",
                                mail = document.getString("mail") ?: "",
                                nutriscore = document.getString("nutriscore") ?: "",
                                novascore = document.getString("novascore") ?: "",
                                ecoscore = document.getString("ecoscore") ?: "",
                                allergens = document.get("allergens") as? MutableList<String> ?: mutableListOf(),
                                allergensTags = document.get("allergens_tags") as? MutableList<String> ?: mutableListOf(),
                                diet = document.getString("diet") ?: "",
                                otherDiets = document.get("other_diets") as? MutableList<String> ?: mutableListOf(),
                                nutriments = document.get("nutriments") as? MutableMap<String, String> ?: mutableMapOf()
                            )
                        }
                    }
                    db.collection("lists").get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                if (document.id == auth.currentUser!!.uid) {
                                    val productLists: MutableList<ProductList> = mutableListOf()
                                    for ((key, value) in document.data) {
                                        if (value is List<*>) {
                                            if (value.isNotEmpty()) {
                                                val list = mutableListOf<Product>()
                                                for (productData in value) {
                                                    if (productData is Map<*, *>) {
                                                        list.add(productData.toProduct())
                                                    }
                                                }
                                                productLists.add(ProductList(key, list))
                                            } else {
                                                productLists.add(ProductList(key, mutableListOf()))
                                            }
                                        }
                                    }
                                    user.lists = productLists
                                }
                            }
                            db.collection("recipes").get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        if (document.id == auth.currentUser!!.uid) {
                                            val recipeLists: MutableList<RecipeList> = mutableListOf()
                                            for ((key, value) in document.data) {
                                                if (value is List<*>) {
                                                    if (value.isNotEmpty()) {
                                                        val list = mutableListOf<Recipe>()
                                                        for (recipeData in value) {
                                                            if (recipeData is Map<*, *>) {
                                                                list.add(recipeData.toRecipe())
                                                            }
                                                        }
                                                        recipeLists.add(RecipeList(key, list))
                                                    } else {
                                                        recipeLists.add(RecipeList(key, mutableListOf()))
                                                    }
                                                }
                                            }
                                            user.recipes = recipeLists
                                        }
                                    }
                                    _userData.postValue(user)
                                }

                        }
                    }
                }
    }

    fun updateUserData(updatedUser: User?) {
        _userData.value = updatedUser ?: _userData.value
    }

    fun editRecipe(
        initList: String,
        selectedList: String,
        recipeId: String,
        name: String,
        ingredientsList: List<String>,
        stepsList: List<String>,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {

            val documentRef = db.collection("recipes").document(auth.currentUser!!.uid)

            db.runTransaction { transaction ->
                val documentSnapshot = transaction.get(documentRef)

                if (selectedList == initList) {

                    val array = documentSnapshot.get(initList) as? MutableList<Map<String, Any>>
                        ?: throw Exception("No se encontró el array en el documento")

                    val indexToUpdate = array.indexOfFirst { it["id"] == recipeId }

                    if (indexToUpdate != -1) {
                        val oldMap = array[indexToUpdate].toMutableMap()
                        oldMap["name"] = name.replaceFirstChar { it.uppercase() }
                        oldMap["ingredients"] = ingredientsList
                        oldMap["steps"] = stepsList
                        array[indexToUpdate] = oldMap
                    } else {
                        throw Exception("No se encontró un mapa con id '$recipeId'")
                    }

                    transaction.update(documentRef, initList, array)
                } else {

                    val oldListArray = documentSnapshot.get(initList) as? MutableList<Map<String, Any>>
                        ?: throw Exception("No se encontró el array en la lista original '$initList'")

                    val indexToDelete = oldListArray.indexOfFirst { it["id"] == recipeId }
                    if (indexToDelete != -1) {
                        oldListArray.removeAt(indexToDelete)
                    } else {
                        throw Exception("No se encontró la receta en la lista original '$initList'")
                    }

                    val selectedListArray = documentSnapshot.get(selectedList) as? MutableList<Map<String, Any>>
                        ?: throw Exception("No se encontró el array en la lista seleccionada '$selectedList'")

                    val newRecipeMap = mapOf(
                        "id" to recipeId,
                        "name" to name.replaceFirstChar { it.uppercase() },
                        "ingredients" to ingredientsList,
                        "steps" to stepsList
                    )

                    selectedListArray.add(newRecipeMap)

                    transaction.update(documentRef, initList, oldListArray)
                    transaction.update(documentRef, selectedList, selectedListArray)
                }

            }.addOnSuccessListener {
                if (selectedList == initList) {
                    val user = userData.value
                    val recipe = user?.recipes?.find { it.name == initList }?.recipes?.find { it.id.toString() == recipeId }
                    if (recipe != null) {
                        val newRecipe = Recipe(
                            recipe.id,
                            name.replaceFirstChar { it.uppercase() },
                            ingredientsList,
                            stepsList
                        )
                        user.recipes.find { it.name == initList }?.recipes?.removeIf { it.id == recipe.id }
                        user.recipes.find { it.name == initList }?.recipes?.add(newRecipe)
                        updateUserData(user)
                    }
                } else {
                    val user = userData.value
                    val recipe = user?.recipes?.find { it.name == initList }?.recipes?.find { it.id.toString() == recipeId }
                    if (recipe != null) {
                        val newRecipe = Recipe(
                            recipe.id,
                            name.replaceFirstChar { it.uppercase() },
                            ingredientsList,
                            stepsList
                        )
                        user.recipes.find { it.name == initList }?.recipes?.removeIf { it.id == recipe.id }
                        user.recipes.find { it.name == selectedList }?.recipes?.add(newRecipe)
                        updateUserData(user)
                    }
                }
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
        } else callback(false)
    }

    fun createRecipe(
        name: String,
        ingredientsList: List<String>,
        stepsList: List<String>,
        selectedList: String,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {

            val newId = UUID.randomUUID().toString()

            val newRecipe = mapOf(
                "id" to newId,
                "name" to name.replaceFirstChar { it.uppercase() },
                "ingredients" to ingredientsList,
                "steps" to stepsList
            )

            db.collection("recipes").document(auth.currentUser!!.uid)
                .update(selectedList, FieldValue.arrayUnion(newRecipe))
                .addOnSuccessListener {
                    val user = userData.value
                    val recipe = Recipe(
                        UUID.fromString(newId),
                        name.replaceFirstChar { it.uppercase() },
                        ingredientsList,
                        stepsList
                    )
                    user?.recipes?.find { it.name == selectedList }?.recipes?.add(recipe)
                    updateUserData(user)
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else callback(false)
    }

    fun saveChatRecipe(
        recipe: Recipe,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {

            val newRecipe = mapOf(
                "id" to recipe.id.toString(),
                "name" to recipe.title,
                "ingredients" to recipe.ingredients,
                "steps" to recipe.steps
            )

            db.collection("recipes").document(auth.currentUser!!.uid)
                .update("Generadas por ChatGPT", FieldValue.arrayUnion(newRecipe))
                .addOnSuccessListener {
                    val user = userData.value
                    user?.recipes?.find { it.name == "Generadas por ChatGPT" }?.recipes?.add(recipe)
                    updateUserData(user)
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else callback(false)
    }

    fun editRecipeList(
        initName: String,
        name: String,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {

            val documentRef = db.collection("recipes").document(auth.currentUser!!.uid)

            db.runTransaction { transaction ->
                val documentSnapshot = transaction.get(documentRef)

                val value = documentSnapshot.get(initName)

                if (value != null) {
                    transaction.update(documentRef, name, value)
                    transaction.update(documentRef, initName, FieldValue.delete())
                } else {
                    throw Exception("El campo '$initName' no existe o su valor es nulo")
                }
            }.addOnSuccessListener {
                val list = userData.value?.recipes?.find { it.name == initName }?.recipes ?: mutableListOf()
                val newList = RecipeList(name, list)
                val updatedUser = userData.value
                updatedUser?.recipes?.removeIf { it.name == initName }
                updatedUser?.recipes?.add(newList)
                updateUserData(updatedUser)
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
        } else callback(false)
    }

    fun createRecipeList(
        name: String,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            val documentRef = db.collection("recipes").document(auth.currentUser!!.uid)
            documentRef.update(mapOf(name to FieldValue.arrayUnion()))
                .addOnSuccessListener {
                    val lists = userData.value?.recipes ?: mutableListOf()
                    val newList = RecipeList(name, mutableListOf())
                    lists.add(newList)
                    val updatedUser = userData.value?.copy(recipes = lists)
                    updateUserData(updatedUser)
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else callback(false)
    }

    fun editSaveList(
        initName: String,
        name: String,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {

            val documentRef = db.collection("lists").document(auth.currentUser!!.uid)

            db.runTransaction { transaction ->
                val documentSnapshot = transaction.get(documentRef)

                val value = documentSnapshot.get(initName)

                if (value != null) {
                    transaction.update(documentRef, name, value)
                    transaction.update(documentRef, initName, FieldValue.delete())
                } else {
                    throw Exception("El campo '$initName' no existe o su valor es nulo")
                }
            }.addOnSuccessListener {
                val list = userData.value?.lists?.find { it.name == initName }?.products ?: mutableListOf()
                val newList = ProductList(name, list)
                val updatedUser = userData.value
                updatedUser?.lists?.removeIf { it.name == initName }
                updatedUser?.lists?.add(newList)
                updateUserData(updatedUser)
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
        } else callback(false)
    }

    fun createSaveList(
        name: String,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            val documentRef = db.collection("lists").document(auth.currentUser!!.uid)
            documentRef.update(mapOf(name to FieldValue.arrayUnion()))
                .addOnSuccessListener {
                    val lists = userData.value?.lists ?: mutableListOf()
                    val newList = ProductList(name, mutableListOf())
                    lists.add(newList)
                    val updatedUser = userData.value?.copy(lists = lists)
                    updateUserData(updatedUser)
                    callback(true)
                }
                .addOnFailureListener { e ->
                    callback(false)
                }
        } else callback(false)
    }

    fun editProfile(
        newData: Map<String, String>,
        name: String,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            db.collection("users").document(auth.currentUser!!.uid).update(newData).addOnSuccessListener {
                val user = userData.value?.copy(name = name)
                updateUserData(user)
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
        } else callback(false)
    }

    fun saveProductToFav(
        product: Product,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            db.collection("lists").document(auth.currentUser!!.uid)
                .update("Favoritos", FieldValue.arrayUnion(product.toMap())).addOnSuccessListener {
                    val lists = userData.value?.lists
                    lists?.find { it.name == "Favoritos" }?.products?.add(product)
                    val user = userData.value?.copy(lists = lists!!)
                    updateUserData(user)
                    callback(true)
                }.addOnFailureListener {
                    callback(false)
                }
        } else callback(false)
    }

    fun removeProductFromFav(
        product: Product,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            db.collection("lists").document(auth.currentUser!!.uid)
                .update("Favoritos", FieldValue.arrayRemove(product.toMap())).addOnSuccessListener {
                    val lists = userData.value?.lists
                    lists?.find { it.name == "Favoritos" }?.products?.removeIf { it.id == product.id }
                    val user = userData.value?.copy(lists = lists!!)
                    updateUserData(user)
                    callback(true)
                }.addOnFailureListener {
                    callback(false)
                }
        } else callback(false)
    }

    fun removeRecipeFromList(
        recipe: Recipe,
        recipeList: RecipeList,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            val docRef = db.collection("recipes").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {

                    val list = document.get(recipeList.name) as? List<Map<String, Any>>

                    if (list != null) {

                        val updatedList = list.filterNot { it["id"] == recipe.id.toString() }

                        docRef.update(recipeList.name, updatedList)
                            .addOnSuccessListener {
                                val userList = userData.value?.recipes ?: mutableListOf()
                                userList.find { it.name == recipeList.name }?.recipes?.removeIf { it.id == recipe.id }
                                val updatedUser = userData.value?.copy(recipes = userList)
                                updateUserData(updatedUser)
                                callback(true)
                            }
                            .addOnFailureListener {
                                callback(false)
                            }
                    }
                }
            }
        } else callback(false)
    }

    fun removeProductFromList(
        product: Product,
        productList: ProductList,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            val docRef = db.collection("lists").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {

                    val list = document.get(productList.name) as? List<Map<String, Any>>

                    if (list != null) {

                        val updatedList = list.filterNot { it["id"] == product.id }

                        docRef.update(productList.name, updatedList)
                            .addOnSuccessListener {
                                val userList = userData.value?.lists ?: mutableListOf()
                                userList.find { it.name == productList.name }?.products?.removeIf { it.id == product.id }
                                val updatedUser = userData.value?.copy(lists = userList)
                                updateUserData(updatedUser)
                                callback(true)
                            }
                            .addOnFailureListener {
                                callback(false)
                            }
                    }
                }
            }
        } else callback(false)
    }

    fun saveToList(
        initialCheckedItems: List<ProductList>,
        checkedItems: List<ProductList>,
        product: Product,
        callback: (Boolean) -> Unit
    ) {
        if (auth.currentUser != null) {
            val userDocRef = db.collection("lists").document(auth.currentUser!!.uid)

            val uncheckedItems = initialCheckedItems.filterNot { it in checkedItems }
            val newCheckedItems = checkedItems.filterNot { it in initialCheckedItems }

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                uncheckedItems.forEach { item ->
                    val array = snapshot.get(item.name) as? List<Map<String, Any>> ?: emptyList()
                    val updatedArray = array.filterNot { it["id"] == product.id }
                    transaction.update(userDocRef, item.name, updatedArray)
                }

                newCheckedItems.forEach { item ->
                    val array = snapshot.get(item.name) as? List<Map<String, Any>> ?: emptyList()
                    val updatedArray = array.toMutableList()
                    if (!array.any { it["id"] == product.id }) updatedArray.add(product.toMap() as Map<String, Any>)
                    transaction.update(userDocRef, item.name, updatedArray)
                }
            }.addOnSuccessListener {
                val user = userData.value
                uncheckedItems.forEach { item ->
                    val products = user?.lists?.find { it.name == item.name }?.products ?: mutableListOf()
                    val updatedArray = products.filterNot { it.id == product.id }
                    user?.lists?.removeIf { it.name == item.name }
                    user?.lists?.add(ProductList(item.name, updatedArray.toMutableList()))
                }
                newCheckedItems.forEach { item ->
                    val productList = user?.lists?.find { it.name == item.name }
                    if (productList != null && productList.products.none { it.id == product.id }) productList.products.add(product)
                    user?.lists?.removeIf { it.name == item.name }
                    if (productList != null) user.lists.add(productList)
                }
                updateUserData(user)
                callback(true)
            }.addOnFailureListener { e ->
                callback(false)
            }
        } else callback(false)
    }

    fun deleteSaveList(productList: ProductList, callback: (Boolean) -> Unit) {
        if (auth.currentUser != null) {

            val documentRef = db.collection("lists").document(auth.currentUser!!.uid)

            documentRef.update(productList.name, FieldValue.delete())
                .addOnSuccessListener {
                    val list = userData.value?.lists ?: mutableListOf()
                    list.removeIf { it.name == productList.name }
                    val updatedUser = userData.value?.copy(lists = list)
                    updateUserData(updatedUser)
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else callback(false)
    }

    fun deleteRecipeList(recipeList: RecipeList, callback: (Boolean) -> Unit) {
        if (auth.currentUser != null) {
            val documentRef = db.collection("recipes").document(auth.currentUser!!.uid)

            documentRef.update(recipeList.name, FieldValue.delete())
                .addOnSuccessListener {
                    val list = userData.value?.recipes ?: mutableListOf()
                    list.removeIf { it.name == recipeList.name }
                    val updatedUser = userData.value?.copy(recipes = list)
                    updateUserData(updatedUser)
                    callback(true)
                }
                .addOnFailureListener {
                   callback(false)
                }
        } else callback(false)
    }

}