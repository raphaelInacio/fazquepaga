---
description: Identifies, analyzes, and starts implementation for the next available task.
---

Using the persona defined in `docs/ai_guidance/personas/task-implementer.md`, I will now begin the process of starting the next task.

First, I will scan the `tasks/prd-{{args}}/` directory to find the next uncompleted task by looking for the first unchecked checkbox in the `_tasks.md` file or the lowest numbered `_task.md` file with a status of `pending`.

Once the task is identified, I will perform the following pre-task setup:
1.  Read the main PRD: `tasks/prd-{{args}}/_prd.md`
2.  Read the Tech Spec: `tasks/prd-{{args}}/_techspec.md`
3.  Read the specific task file (e.g., `tasks/prd-{{args}}/[task_num]_task.md`)

After this analysis, I will provide a summary of the task and a plan for implementation. I will then proceed with implementing the task.

PRD Slug: {{args}}
