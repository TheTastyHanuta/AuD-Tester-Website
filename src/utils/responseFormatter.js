const SubmissionStatus = require('./submissionStatus');

function formatSubmissionResponse(result) {
  // Common fields to preserve
  const response = {
    details: result.details,
    points: result.points,
    deadline: result.deadline,
    encodingWarning: result.encodingWarning,
    deadlinePassed: result.deadlinePassed,
  };

  switch (result.status) {
    case SubmissionStatus.SUCCESS:
      return {
        ...response,
        success: true,
        status: '✅',
        message:
          'Alles supi. Du kannst die Dateien so auf StudOn hochladen. Genaueres Feedback wird angezeigt, wenn die Deadline vorbei ist.',
      };

    case SubmissionStatus.COMPILATION_ERROR:
      return {
        ...response,
        success: false,
        status: '❌',
        message:
          'Compile Error. Bitte überprüfe Deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen)',
      };

    case SubmissionStatus.TEST_FAILED:
      return {
        ...response,
        success: false,
        status: '❌',
        message:
          'Compile Error. Bitte überprüfe Deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen)',
      };

    case SubmissionStatus.SYSTEM_ERROR:
      return {
        ...response,
        success: false,
        status: '⚠️',
        message:
          'Internal Error. Das ist gar nicht gut. Wenn das öfter passiert, melde Dich bitte im Forum.',
      };

    case SubmissionStatus.INVALID_EXERCISE:
      return {
        ...response,
        success: false,
        status: '❌',
        message: 'Ungültige Übung ausgewählt',
      };

    case SubmissionStatus.INVALID_FILES:
      return {
        ...response,
        success: false,
        status: '❌',
        message: 'Ungültige Dateiauswahl',
      };

    default:
      return {
        ...response,
        success: false,
        status: '⚠️',
        message: 'Unbekannter Fehler aufgetreten.',
      };
  }
}

module.exports = { formatSubmissionResponse };
