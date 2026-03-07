/**
 * PrepHunt - Create Test Page JavaScript
 * Handles sidebar collapsing and interactive elements
 */

document.addEventListener('DOMContentLoaded', function () {
    // Initialize collapsible navigation items
    initializeCollapsibleNav();

    // Add hover effects to test cards
    initializeTestCards();

    // Mobile sidebar toggle (if needed)
    initializeMobileSidebar();
});

/**
 * Initialize collapsible navigation groups
 */
function initializeCollapsibleNav() {
    const collapsibleItems = document.querySelectorAll('.nav-item.collapsible');

    collapsibleItems.forEach(item => {
        item.addEventListener('click', function (e) {
            e.preventDefault();
            const navGroup = this.closest('.nav-group');
            const subitems = navGroup.querySelector('.nav-subitems');

            if (subitems) {
                const isExpanded = subitems.style.maxHeight;

                if (isExpanded) {
                    subitems.style.maxHeight = null;
                    subitems.style.opacity = '0';
                } else {
                    subitems.style.maxHeight = subitems.scrollHeight + 'px';
                    subitems.style.opacity = '1';
                }
            }
        });
    });

    // Auto-expand active groups
    const activeSubitems = document.querySelectorAll('.nav-subitem.active');
    activeSubitems.forEach(subitem => {
        const navGroup = subitem.closest('.nav-group');
        const subitems = navGroup.querySelector('.nav-subitems');
        if (subitems) {
            subitems.style.maxHeight = subitems.scrollHeight + 'px';
            subitems.style.opacity = '1';
        }
    });
}

/**
 * Add interactive effects to test cards
 */
function initializeTestCards() {
    const testCards = document.querySelectorAll('.test-card');

    testCards.forEach(card => {
        // Add ripple effect on click
        card.addEventListener('click', function (e) {
            const ripple = document.createElement('div');
            ripple.className = 'ripple';

            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';

            this.appendChild(ripple);

            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

/**
 * Initialize mobile sidebar toggle
 */
function initializeMobileSidebar() {
    // Create hamburger menu button for mobile
    if (window.innerWidth <= 768) {
        const menuButton = document.createElement('button');
        menuButton.className = 'mobile-menu-btn';
        menuButton.innerHTML = `
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
        `;

        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.insertBefore(menuButton, mainContent.firstChild);
        }

        menuButton.addEventListener('click', function () {
            const sidebar = document.querySelector('.sidebar');
            if (sidebar) {
                sidebar.classList.toggle('active');
            }
        });

        // Close sidebar when clicking outside
        document.addEventListener('click', function (e) {
            const sidebar = document.querySelector('.sidebar');
            const menuBtn = document.querySelector('.mobile-menu-btn');

            if (sidebar && sidebar.classList.contains('active') &&
                !sidebar.contains(e.target) &&
                !menuBtn.contains(e.target)) {
                sidebar.classList.remove('active');
            }
        });
    }
}

/**
 * Smooth scroll animation for navigation
 */
function smoothScroll(target) {
    const element = document.querySelector(target);
    if (element) {
        element.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

/**
 * Add loading state to buttons
 */
function addButtonLoadingState(button) {
    button.disabled = true;
    button.classList.add('loading');
    const originalText = button.textContent;
    button.textContent = 'Loading...';

    return function removeLoadingState() {
        button.disabled = false;
        button.classList.remove('loading');
        button.textContent = originalText;
    };
}

// Additional CSS for ripple effect and mobile menu
const additionalStyles = document.createElement('style');
additionalStyles.textContent = `
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        transform: scale(0);
        animation: ripple-animation 0.6s ease-out;
        pointer-events: none;
    }
    
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .mobile-menu-btn {
        display: none;
        position: fixed;
        top: 1rem;
        left: 1rem;
        z-index: 1000;
        background: var(--color-primary);
        border: none;
        border-radius: var(--radius-md);
        width: 48px;
        height: 48px;
        cursor: pointer;
        box-shadow: var(--shadow-lg);
        transition: var(--transition-base);
    }
    
    .mobile-menu-btn svg {
        width: 24px;
        height: 24px;
        color: white;
    }
    
    .mobile-menu-btn:hover {
        transform: scale(1.05);
    }
    
    @media (max-width: 768px) {
        .mobile-menu-btn {
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .sidebar {
            box-shadow: var(--shadow-lg);
        }
    }
    
    .nav-subitems {
        max-height: 0;
        opacity: 0;
        overflow: hidden;
        transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1),
                    opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .btn-primary.loading {
        opacity: 0.7;
        cursor: not-allowed;
    }
`;

document.head.appendChild(additionalStyles);

let questionCount = 0;
let currentDifficulty = 'easy';
let currentCourse = 'Choose a course';

// Open Modal
function openTestModal() {
    document.getElementById('testModal').classList.add('active');
    document.body.style.overflow = 'hidden';

    // Reset and add first question
    questionCount = 0;
    document.getElementById('questionsContainer').innerHTML = '';
    addQuestion();
}

// Close Modal
function closeTestModal() {
    document.getElementById('testModal').classList.remove('active');
    document.body.style.overflow = 'auto';

    // Reset form
    document.getElementById('testTitle').value = '';
    document.getElementById('questionsContainer').innerHTML = '';
    questionCount = 0;
}

// Toggle Dropdown
function toggleDropdown(dropdownId) {
    const dropdown = document.getElementById(dropdownId);
    const allDropdowns = document.querySelectorAll('.dropdown-menu-custom');

    allDropdowns.forEach(d => {
        if (d.id !== dropdownId) {
            d.classList.remove('active');
        }
    });

    dropdown.classList.toggle('active');
}

// Set Difficulty
function setDifficulty(level) {
    currentDifficulty = level;
    const indicator = document.getElementById('difficultyIndicator');
    const text = document.getElementById('difficultyText');

    indicator.className = `difficulty-indicator ${level}`;
    text.textContent = level.charAt(0).toUpperCase() + level.slice(1);

    toggleDropdown('difficultyDropdown');
}

// Set Course
function setCourse(course) {
    currentCourse = course;
    document.getElementById('courseText').textContent = course;
    toggleDropdown('courseDropdown');
}

// Add Question
function addQuestion() {
    questionCount++;

    const questionCard = document.createElement('div');
    questionCard.className = 'question-card';
    questionCard.id = `question-${questionCount}`;

    questionCard.innerHTML = `
            <div class="question-header">
                <h3 class="question-number">Question ${questionCount}</h3>
                ${questionCount > 1 ? `<button class="delete-question-btn" onclick="deleteQuestion(${questionCount})">
                    <i class="bi bi-trash" style="font-size: 18px;"></i>
                </button>` : ''}
            </div>
    
            <input type="text" class="question-input" placeholder="Enter your question here..." data-question="${questionCount}">
    
            <h4 class="answer-section-title">Answer ${questionCount}</h4>
    
            <div class="options-container" id="options-${questionCount}">
                <div class="option-item">
                    <div class="option-radio" onclick="selectOption(${questionCount}, 1)"></div>
                    <input type="text" class="option-input" placeholder="Option 1" data-option="${questionCount}-1">
                </div>
                <div class="option-item">
                    <div class="option-radio" onclick="selectOption(${questionCount}, 2)"></div>
                    <input type="text" class="option-input" placeholder="Option 2" data-option="${questionCount}-2">
                </div>
                <div class="option-item">
                    <div class="option-radio" onclick="selectOption(${questionCount}, 3)"></div>
                    <input type="text" class="option-input" placeholder="Option 3" data-option="${questionCount}-3">
                </div>
                <div class="option-item">
                    <div class="option-radio" onclick="selectOption(${questionCount}, 4)"></div>
                    <input type="text" class="option-input" placeholder="Option 4" data-option="${questionCount}-4">
                </div>
            </div>
        `;

    document.getElementById('questionsContainer').appendChild(questionCard);

    // Scroll to new question
    setTimeout(() => {
        questionCard.scrollIntoView({behavior: 'smooth', block: 'nearest'});
    }, 100);
}

// Delete Question
function deleteQuestion(questionId) {
    const questionCard = document.getElementById(`question-${questionId}`);
    if (questionCard) {
        questionCard.remove();

        // Renumber remaining questions
        const allQuestions = document.querySelectorAll('.question-card');
        allQuestions.forEach((card, index) => {
            const newNumber = index + 1;
            card.id = `question-${newNumber}`;

            const numberElement = card.querySelector('.question-number');
            if (numberElement) {
                numberElement.textContent = `Question ${newNumber}`;
            }

            const answerTitle = card.querySelector('.answer-section-title');
            if (answerTitle) {
                answerTitle.textContent = `Answer ${newNumber}`;
            }
        });

        questionCount = allQuestions.length;
    }
}

// Select Option
function selectOption(questionId, optionId) {
    // Deselect all options in this question
    const allRadios = document.querySelectorAll(`#question-${questionId} .option-radio`);
    allRadios.forEach(radio => radio.classList.remove('selected'));

    // Select clicked option
    const selectedRadio = document.querySelector(`#question-${questionId} .option-item:nth-child(${optionId}) .option-radio`);
    if (selectedRadio) {
        selectedRadio.classList.add('selected');
    }
}

// Save Test
function saveTest() {
    const testTitle = document.getElementById('testTitle').value;

    if (!testTitle.trim()) {
        alert('Please enter a test title');
        return;
    }

    const questions = [];
    const questionCards = document.querySelectorAll('.question-card');

    questionCards.forEach((card, index) => {
        const questionNumber = index + 1;
        const questionText = card.querySelector('.question-input').value;

        const options = [];
        const optionInputs = card.querySelectorAll('.option-input');
        optionInputs.forEach((input, optIndex) => {
            options.push({
                optionNumber: optIndex + 1,
                optionText: input.value
            });
        });

        const selectedRadio = card.querySelector('.option-radio.selected');
        const correctAnswer = selectedRadio
            ? Array.from(card.querySelectorAll('.option-radio')).indexOf(selectedRadio) + 1
            : null;

        questions.push({
            questionNumber: questionNumber,
            questionText: questionText,
            options: options,
            correctAnswer: correctAnswer
        });
    });

    const testData = {
        title: testTitle,
        difficulty: currentDifficulty,
        course: currentCourse,
        questions: questions
    };

    console.log('Test Data:', testData);

    // Here you would send testData to your backend
    // For now, we'll just show an alert
    alert('Test saved successfully!');
    closeTestModal();
}

function openUploadOptions() {
    var modal = new bootstrap.Modal(document.getElementById('uploadModal'));
    modal.show();
}

async function uploadPDF(button) {

    const courseId = button.getAttribute("data-course-id");

    if (!courseId) {
        console.error("Course ID is missing!");
        return;
    }
    else{
        console.log("course id is " + courseId);
    }

    const fileInput = document.getElementById("pdfFile");

    if (fileInput.files.length === 0) {
        return;
    }

    const file = fileInput.files[0];

    const formData = new FormData();
    formData.append("file", file);
    formData.append("courseId", courseId);

    console.log("Sending courseId:", courseId); // DEBUG

    // Close dropdown safely
    const toggle = button.closest('.dropdown')?.querySelector('[data-bs-toggle="dropdown"]');
    const dropdown = toggle ? bootstrap.Dropdown.getInstance(toggle) : null;
    if (dropdown) dropdown.hide();

    // Show toast
    const toastEl = document.getElementById("uploadToast");
    const toast = new bootstrap.Toast(toastEl);
    toast.show();

    const progressBar = document.getElementById("uploadProgress");
    const toastMessage = document.getElementById("toastMessage");

    toastMessage.innerText = "Uploading PDF...";
    progressBar.style.width = "30%";

    try {

        const response = await fetch("/upload-drive", {
            method: "POST",
            body: formData
        });

        progressBar.style.width = "100%";

        if (response.ok) {
            toastMessage.innerText = "PDF uploaded successfully!";
            setTimeout(() => location.reload(), 1000);
        } else {
            const errorText = await response.text();
            console.error("Server error:", errorText);
            toastMessage.innerText = "Upload failed.";
        }

    } catch (error) {
        console.error("Network error:", error);
        toastMessage.innerText = "Upload failed (network error).";
    }

    setTimeout(() => {
        toast.hide();
        progressBar.style.width = "0%";
    }, 2500);
}

// function uploadPDF(button, courseId) {
//     const fileInput = document.getElementById("pdfFile");
//     if (fileInput.files.length === 0) return;
//
//     const file = fileInput.files[0];
//     const formData = new FormData();
//     formData.append("file", file);
//     formData.append("courseId", courseId);
//
//     // Close dropdown
//     const dropdown = bootstrap.Dropdown.getInstance(
//         button.closest('.dropdown').querySelector('[data-bs-toggle="dropdown"]')
//     );
//     dropdown.hide();
//
//     // Show toast
//     const toastEl = document.getElementById("uploadToast");
//     const toast = new bootstrap.Toast(toastEl);
//     toast.show();
//
//     const progressBar = document.getElementById("uploadProgress");
//     const toastMessage = document.getElementById("toastMessage");
//
//     toastMessage.innerText = "Uploading PDF...";
//     progressBar.style.width = "0%";
//
//     // Use XMLHttpRequest to track upload progress
//     const xhr = new XMLHttpRequest();
//     xhr.open("POST", "/upload-drive", true);
//
//     // Update progress bar
//     xhr.upload.onprogress = function(e) {
//         if (e.lengthComputable) {
//             const percent = (e.loaded / e.total) * 100;
//             progressBar.style.width = percent + "%";
//         }
//     };
//
//     // When upload is done
//     xhr.onload = function() {
//         if (xhr.status === 200) {
//             toastMessage.innerText = "PDF uploaded successfully!";
//             progressBar.style.width = "100%";
//
//             // reload after 1s to show new PDF
//             setTimeout(() => location.reload(), 1000);
//         } else {
//             toastMessage.innerText = "Upload failed. Try again.";
//         }
//
//         // hide toast after 2.5s
//         setTimeout(() => {
//             toast.hide();
//             progressBar.style.width = "0%";
//         }, 2500);
//     };
//
//     // Handle network error
//     xhr.onerror = function() {
//         toastMessage.innerText = "Network error. Upload failed.";
//         setTimeout(() => {
//             toast.hide();
//             progressBar.style.width = "0%";
//         }, 2500);
//     };
//
//     xhr.send(formData);
// }

// Close dropdowns when clicking outside
document.addEventListener('click', function (event) {
    if (!event.target.closest('.difficulty-dropdown') && !event.target.closest('.course-dropdown')) {
        document.querySelectorAll('.dropdown-menu-custom').forEach(d => {
            d.classList.remove('active');
        });
    }
});

// Close modal when clicking outside
document.getElementById('testModal').addEventListener('click', function (event) {
    if (event.target === this) {
        closeTestModal();
    }
});

function createCourse() {

    const courseName = document.getElementById("courseNameInput").value;
    const courseDesc = document.getElementById("courseDescInput").value;

    fetch("/create-course", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
            courseName: courseName,
            courseDesc: courseDesc
        })
    })
        .then(response => response.json())
        .then(data => {
            location.reload();
        });
}