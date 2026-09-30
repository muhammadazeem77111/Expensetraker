package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteIncomeById(id: Long): Int

    @Query("SELECT * FROM income WHERE id = :id LIMIT 1")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("SELECT * FROM income ORDER BY date DESC, createdAt DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, createdAt DESC")
    fun getIncomeForDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM income")
    fun getTotalIncome(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM income WHERE date >= :startDate AND date <= :endDate")
    fun getTotalIncomeForDateRange(startDate: Long, endDate: Long): Flow<Long>

    @Query("DELETE FROM income")
    suspend fun clearAllIncome()
}
