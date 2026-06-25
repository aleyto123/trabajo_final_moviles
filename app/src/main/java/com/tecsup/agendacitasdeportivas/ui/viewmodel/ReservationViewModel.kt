package com.tecsup.agendacitasdeportivas.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.MPBackUrls
import com.tecsup.agendacitasdeportivas.data.network.MPItem
import com.tecsup.agendacitasdeportivas.data.network.MPPreferenceRequest
import com.tecsup.agendacitasdeportivas.data.network.RetrofitClient
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.ui.state.DetailUiState
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReservationViewModel(
    private val repository: CanchaReservationRepository
) : ViewModel() {

    // Estado de la UI para la lista (Consumo de Room en tiempo real)
    val uiState: StateFlow<ReservationUiState> = repository.allReservations
        .map<List<CanchaReservationEntity>, ReservationUiState> { reservations ->
            ReservationUiState.Success(reservations)
        }
        .onStart { emit(ReservationUiState.Loading) }
        .catch { e -> emit(ReservationUiState.Error(e.message ?: "Error al cargar reservas")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReservationUiState.Loading
        )

    // Estado de la UI para el detalle
    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    // Estados para el Formulario (Control Bidireccional)
    var customerName by mutableStateOf("")
    var canchaType by mutableStateOf("")
    var reservationDate by mutableStateOf("")
    var selectedTimes by mutableStateOf(setOf<String>())
    var hourlyPrice by mutableDoubleStateOf(0.0)
    var paymentStatus by mutableStateOf("Pendiente")
    
    var editingReservationId by mutableStateOf<String?>(null)
    var lastSavedId by mutableStateOf<String?>(null)

    // Mercado Pago Web States
    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _isProcessingPayment = MutableStateFlow(false)
    val isProcessingPayment: StateFlow<Boolean> = _isProcessingPayment.asStateFlow()

    var paymentErrorMessage by mutableStateOf<String?>(null)

    // Validación básica
    val isFormValid: Boolean
        get() = customerName.isNotBlank() && 
                canchaType.isNotBlank() && 
                reservationDate.isNotBlank() && 
                selectedTimes.isNotEmpty()

    fun toggleTimeSlot(time: String) {
        selectedTimes = if (selectedTimes.contains(time)) {
            selectedTimes - time
        } else {
            selectedTimes + time
        }
    }

    fun clearForm() {
        customerName = ""
        canchaType = ""
        reservationDate = ""
        selectedTimes = emptySet()
        hourlyPrice = 0.0
        paymentStatus = "Pendiente"
        editingReservationId = null
    }

    fun prepareFormForEdit(reservation: CanchaReservationEntity, baseHourlyPrice: Double) {
        editingReservationId = reservation.id
        customerName = reservation.customerName
        canchaType = reservation.canchaType
        reservationDate = reservation.reservationDate
        selectedTimes = reservation.reservationTime.split(", ").toSet()
        hourlyPrice = baseHourlyPrice
        paymentStatus = reservation.paymentStatus
    }

    fun loadReservation(id: String) {
        viewModelScope.launch {
            try {
                _detailState.value = DetailUiState.Loading
                val reservation = repository.getReservationById(id)
                if (reservation != null) {
                    _detailState.value = DetailUiState.Success(reservation)
                } else {
                    _detailState.value = DetailUiState.Error("Reserva no encontrada")
                }
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun save() {
        if (!isFormValid) return
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "ANONYMOUS"
                val idToUse = editingReservationId ?: java.util.UUID.randomUUID().toString()
                val reservation = CanchaReservationEntity(
                    id = idToUse,
                    userId = currentUserId,
                    canchaType = canchaType,
                    customerName = customerName,
                    reservationDate = reservationDate,
                    reservationTime = selectedTimes.toList().sorted().joinToString(", "),
                    hourlyPrice = hourlyPrice * selectedTimes.size,
                    paymentStatus = paymentStatus
                )
                if (editingReservationId == null) {
                    repository.insertReservation(reservation)
                } else {
                    repository.updateReservation(reservation)
                }
                lastSavedId = idToUse
                clearForm()
            } catch (e: Exception) {
                // Error handled by repository abstraction ideally
            }
        }
    }

    fun update(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            try {
                repository.updateReservation(reservation)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun delete(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            try {
                repository.deleteReservation(reservation)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // --- FLUJO DE PAGO WEB (SIN SDK) ---

    fun generatePaymentLink(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            _isProcessingPayment.value = true
            paymentErrorMessage = null
            try {
                val request = MPPreferenceRequest(
                    items = listOf(
                        MPItem(
                            title = "Reserva de Cancha - ${reservation.canchaType}",
                            unit_price = reservation.hourlyPrice
                        )
                    ),
                    back_urls = MPBackUrls(
                        success = "https://canchalibre-6d670.web.app",
                        pending = "https://canchalibre-6d670.web.app",
                        failure = "https://canchalibre-6d670.web.app"
                    )
                )
                
                // IMPORTANTE: Usando el Access Token real proporcionado por el usuario
                val response = RetrofitClient.paymentApi.createPreference(
                    "Bearer APP_USR-8414987955455701-062411-6e5013d646388415d5a4ec47186ecb33-3496556980",
                    request
                )
                
                if (response.init_point.isNotEmpty()) {
                    _paymentUrl.value = response.init_point
                } else {
                    paymentErrorMessage = "No se pudo generar el link de pago (init_point vacío)."
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("PaymentWeb", "Error HTTP al crear link de pago: ${e.code()} - ${e.response()?.errorBody()?.string()}", e)
                paymentErrorMessage = when (e.code()) {
                    401 -> "Token Inválido: Asegúrese de usar un Access Token de Mercado Pago real."
                    400 -> "Error en Pedido: Mercado Pago rechazó los datos del pago."
                    else -> "Error ${e.code()}: No se pudo conectar con Mercado Pago."
                }
            } catch (e: java.io.IOException) {
                Log.e("PaymentWeb", "Error de red: ${e.message}", e)
                paymentErrorMessage = "Error de Red: Verifique su conexión a internet."
            } catch (e: Exception) {
                Log.e("PaymentWeb", "Error desconocido: ${e.message}", e)
                paymentErrorMessage = "Error inesperado al generar el pago."
            } finally {
                _isProcessingPayment.value = false
            }
        }
    }

    fun clearPaymentLink() {
        _paymentUrl.value = null
    }

    /**
     * Inicia el listener para detectar el pago y disparar la notificación Push V1.
     * Se recomienda llamar a esto antes de abrir el link de Mercado Pago.
     */
    fun startPaymentNotificationListener(reservationId: String) {
        viewModelScope.launch {
            try {
                // Obtenemos el token FCM del dispositivo actual
                val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                repository.escucharPagoYDispararPush(reservationId, token)
            } catch (e: Exception) {
                Log.e("FCM_V1", "No se pudo obtener el token del dispositivo", e)
            }
        }
    }

    fun onPaymentSuccess(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            repository.completePayment(reservation)
        }
    }
}
