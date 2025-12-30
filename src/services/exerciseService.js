const fs = require('fs-extra');
const path = require('path');
const logger = require('../../logger');

const EXERCISES_FILE = path.join(__dirname, '../../exercises.json');

async function getAllExercises() {
  try {
    return await fs.readJson(EXERCISES_FILE);
  } catch (error) {
    logger.error('Error reading exercises file', { error: error.message });
    throw new Error('Failed to read exercises');
  }
}

async function updateExerciseDeadline(exerciseId, newDeadline) {
  try {
    const exercises = await getAllExercises();
    const exerciseIndex = exercises.findIndex(ex => ex.id === exerciseId);

    if (exerciseIndex === -1) {
      throw new Error('Exercise not found');
    }

    // Validate deadline format (ISO 8601)
    const deadlineDate = new Date(newDeadline);
    if (isNaN(deadlineDate.getTime())) {
      throw new Error('Invalid deadline format');
    }

    exercises[exerciseIndex].deadline = newDeadline;

    // Write back to file with pretty formatting
    await fs.writeJson(EXERCISES_FILE, exercises, { spaces: 2 });

    logger.info('Exercise deadline updated', {
      exerciseId,
      newDeadline,
      exercise: exercises[exerciseIndex].name,
    });

    return exercises[exerciseIndex];
  } catch (error) {
    logger.error('Error updating exercise deadline', {
      error: error.message,
      exerciseId,
      newDeadline,
    });
    throw error;
  }
}

module.exports = {
  getAllExercises,
  updateExerciseDeadline,
};
