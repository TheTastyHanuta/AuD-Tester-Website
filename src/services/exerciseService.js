const fs = require('fs-extra');
const path = require('path');
const logger = require('../../logger');

const EXERCISES_FILE = path.join(__dirname, '../../exercises.json');
let exerciseWriteQueue = Promise.resolve();

async function getAllExercises() {
  try {
    return await fs.readJson(EXERCISES_FILE);
  } catch (error) {
    logger.error('Error reading exercises file', { error: error.message });
    throw new Error('Failed to read exercises');
  }
}

function normalizeDeadline(deadline) {
  if (typeof deadline !== 'string' || deadline.trim() === '') {
    throw new Error('Invalid deadline format');
  }

  const deadlineDate = new Date(deadline);
  if (isNaN(deadlineDate.getTime())) {
    throw new Error('Invalid deadline format');
  }

  return deadlineDate.toISOString();
}

async function writeExercisesAtomically(exercises) {
  const tempFile = `${EXERCISES_FILE}.${process.pid}.${Date.now()}.tmp`;
  await fs.writeJson(tempFile, exercises, { spaces: 2 });
  await fs.rename(tempFile, EXERCISES_FILE);
}

function withExerciseWriteLock(task) {
  const run = exerciseWriteQueue.then(task, task);
  exerciseWriteQueue = run.catch(() => {});
  return run;
}

async function updateExerciseDeadlines(updates) {
  return withExerciseWriteLock(async () => {
    if (!Array.isArray(updates) || updates.length === 0) {
      throw new Error('At least one deadline update is required');
    }

    const exercises = await getAllExercises();
    const normalizedUpdates = updates.map(update => {
      if (!update || typeof update.id !== 'string') {
        throw new Error('Exercise id is required');
      }

      return {
        id: update.id,
        deadline: normalizeDeadline(update.deadline),
      };
    });

    const seenIds = new Set();
    for (const update of normalizedUpdates) {
      if (seenIds.has(update.id)) {
        throw new Error(`Duplicate exercise update: ${update.id}`);
      }
      seenIds.add(update.id);

      if (!exercises.some(ex => ex.id === update.id)) {
        throw new Error(`Exercise not found: ${update.id}`);
      }
    }

    const updateById = new Map(
      normalizedUpdates.map(update => [update.id, update.deadline])
    );

    const updatedExercises = exercises.map(exercise =>
      updateById.has(exercise.id)
        ? { ...exercise, deadline: updateById.get(exercise.id) }
        : exercise
    );

    await writeExercisesAtomically(updatedExercises);

    logger.info('Exercise deadlines updated', {
      count: normalizedUpdates.length,
      exerciseIds: normalizedUpdates.map(update => update.id),
    });

    return updatedExercises;
  }).catch(error => {
    logger.error('Error updating exercise deadlines', {
      error: error.message,
      updates,
    });
    throw error;
  });
}

async function updateExerciseDeadline(exerciseId, newDeadline) {
  const exercises = await updateExerciseDeadlines([
    { id: exerciseId, deadline: newDeadline },
  ]);
  return exercises.find(ex => ex.id === exerciseId);
}

module.exports = {
  getAllExercises,
  updateExerciseDeadline,
  updateExerciseDeadlines,
};
