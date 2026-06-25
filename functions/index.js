const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Cloud Function que se dispara cuando una reserva se actualiza.
 * Si el estado cambia a 'pagado', sincroniza el campo paymentStatus y envía un Push.
 */
exports.enviarPushConfirmacion = onDocumentUpdated("reservas/{reservaId}", async (event) => {
    const dataBefore = event.data.before.data();
    const dataAfter = event.data.after.data();

    // 1. Verificamos si el estado cambió a 'pagado'
    if (dataBefore.estado !== "pagado" && dataAfter.estado === "pagado") {

        // 2. SINCRONIZACIÓN: Actualizamos paymentStatus para que la app lo vea correctamente
        if (dataAfter.paymentStatus !== "Pagado") {
            try {
                await event.data.after.ref.update({
                    paymentStatus: "Pagado"
                });
                console.log("Campo paymentStatus sincronizado a Pagado");
            } catch (error) {
                console.error("Error al sincronizar paymentStatus:", error);
            }
        }

        // 3. ENVÍO DE NOTIFICACIÓN PUSH
        const token = dataAfter.fcmToken;
        if (token) {
            const message = {
                notification: {
                    title: "¡Pago Confirmado! ⚽",
                    body: `Tu reserva para ${dataAfter.canchaType} ha sido pagada con éxito. ¡Prepárate para el partido!`
                },
                token: token
            };

            try {
                const response = await getMessaging().send(message);
                console.log("Push enviado correctamente:", response);
            } catch (error) {
                console.error("Error enviando push:", error);
            }
        }
    }

    return null;
});
