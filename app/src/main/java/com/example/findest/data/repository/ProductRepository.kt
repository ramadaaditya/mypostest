package com.example.findest.data.repository

import com.example.findest.data.local.ProductDao
import com.example.findest.data.model.Product
import com.example.findest.data.model.ProductInCart
import com.example.findest.data.remote.api.ApiService
import com.example.findest.utils.UiState
import com.example.findest.utils.toProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val dao: ProductDao
) {
    suspend fun getAllProducts(): UiState<List<Product>> {
        return try {
            val response = apiService.getAllProducts()
            val products = response.products?.mapNotNull { it?.toProductEntity() } ?: emptyList()
            dao.insertAllProduct(products)
            UiState.Success(products)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui")
        }
    }

    suspend fun getDetailProduct(id: Int): UiState<Product> {
        val local = dao.getProductById(id).first()
        return if (local != null) {
            UiState.Success(local)
        } else {
            try {
                val response = apiService.getDetailProduct(id)
                val product = response.product
                if (product != null) {
                    val entity = product.toProductEntity()
                    dao.insertProductItem(entity)
                    UiState.Success(entity)
                } else {
                    UiState.Error("Produk tidak ditemukan")
                }
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui")
            }
        }
    }

    suspend fun insertCartItem(item: ProductInCart) = dao.insertCartItem(item)
    fun getAllProductsFromLocal(): Flow<List<Product>> = dao.getAllProducts()
    suspend fun insertAllProductsToLocal(products: List<Product>) = dao.insertAllProduct(products)
    fun getAllCartItems() = dao.getAllCartItems()
    suspend fun deleteCartItem(item: ProductInCart) = dao.deleteCartItem(item)
    suspend fun deleteAllCartItem() = dao.deleteAllCartItem()
}