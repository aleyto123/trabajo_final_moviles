const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Cloud Function que se dispara cuando una reserva se actualiza.
 * Si el estado cambia de 'pendiente' a 'pagado', envía una notificación push al usuario.
 */
exports.enviarPushConfirmacion = onDocumentUpdated("reservas/{reservaId}", async (event) => {
    const dataBefore = event.data.before.data();
    const dataAfter = event.data.after.data();

    // Verificamos si hubo un cambio de estado a 'pagado'
    if (dataBefore.estado !== "pagado" && dataAfter.estado === "pagado") {
        const token = dataAfter.fcmToken;

        if (!token) {
            console.log("No hay fcmToken en el documento: " + event.params.reservaId);
            return null;
        }

        const message = {
            notification: {
                title: "¡Reserva Confirmada! ⚽",
                body: `Tu pago para la cancha ${dataAfter.canchaType} fue exitoso. ¡Te esperamos!`
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

    return null;
});
