# Assignment: Accessibility understanding

**Name:** Sandip Ranjit

**Date:** 02/04/2026

### What does WCAG stand for?
WCAG stands for Web Content Accessibility Guidelines. 
It’s a set of standards created to make web content more accessible to people with disabilities, 
developed by the World Wide Web Consortium.

WCAG has three levels of conformance, each representing increasing accessibility:

- Level A (minimum)
The most basic requirements. If not met, some users may not be able to access content at all.

- Level AA (standard)
The most commonly targeted level. Covers the biggest accessibility barriers and is often required by laws and regulations.

- Level AAA (highest)
The most advanced level. Provides the highest accessibility but is not always practical to achieve for all content.

**The Four WCAG Principles (POUR)**

WCAG is organized around four core principles, often remembered by the acronym POUR:

1. Perceivable
Information must be presented in ways users can perceive (e.g., text alternatives for images, captions for audio).

2. Operable
Users must be able to navigate and interact with the interface (e.g., keyboard accessibility, no content that causes seizures).

3. Understandable
Information and UI should be clear and predictable (e.g., readable text, consistent navigation, error guidance).

4. Robust
Content must work reliably across different technologies, including assistive technologies (like screen readers).

---

## Accessibility Audit Task

### Tools used:
The following tools were used to evaluate website accessibility:
- Accessibility Insights for Web (Microsoft)
- WAVE Evaluation Tool (WebAIM)

### Summary of Findings
The evaluation identified several accessibility issues that impact usability, particularly for users relying on assistive technologies or those with visual impairments.

### Identified Issues

### 1. Low Color Contrast
- **Description:** Some text elements do not meet WCAG contrast requirements, making them difficult to read.
- **Impact:** Users with visual impairments or color vision deficiencies may struggle to read content.
- **WCAG Reference:** WCAG 2.1 – 1.4.3 Contrast (Minimum)
- **Recommendation:** Increase contrast between text and background to meet minimum contrast ratios.

### 2. Missing Alt Text for Images
- **Description:** Some images do not have alternative (alt) text.
- **Impact:** Screen reader users cannot understand the content or purpose of images.
- **WCAG Reference:** WCAG 2.1 – 1.1.1 Non-text Content
- **Recommendation:** Add descriptive alt text to all meaningful images. Use empty alt attributes (`alt=""`) for decorative images.

### 3. Missing or Improper Link Text
- **Description:** Some links do not have descriptive text (e.g., “click here” or empty links).
- **Impact:** Screen reader users cannot determine the purpose of links.
- **WCAG Reference:** WCAG 2.1 – 2.4.4 Link Purpose (In Context)
- **Recommendation:** Ensure all links have clear, descriptive text that explains their purpose.

### 4. Missing Buttons or ARIA Labels
- **Description:** Some interactive elements (buttons) are missing accessible names or ARIA labels.
- **Impact:** Screen readers cannot properly identify or announce the purpose of these elements.
- **WCAG Reference:** WCAG 2.1 – 4.1.2 Name, Role, Value
- **Recommendation:** Provide accessible names using:
    - Visible text labels
    - `aria-label` or `aria-labelledby` attributes where necessary

### Overall Assessment
The websites partially met accessibility standards but contained several issues that prevent it from fully complying with WCAG guidelines.
Addressing the identified problems will significantly improve usability and inclusivity.

### Suggestions
- Fix color contrast issues
- Add alt text to all images
- Improve link descriptions
- Ensure all interactive elements have accessible names
- Re-test the site after fixes using accessibility tools

---

## Physical Accessibility Task – Metropolia Premises

### Objective
The purpose of this task was to identify key physical accessibility features within Metropolia premises, including:
- Surface textures for hand guidance
- Surface textures for foot guidance
- Use of braille
- Accessibility of Inva WC door widths according to Finnish guidelines

### Observations

**1. Texture on Surfaces for Hand**
- Textured elements were observed on **door handles**, which assist in identifying and operating doors through touch.
- These textures help users with visual impairments distinguish and interact with entry points more easily.

**2. Texture on Surfaces for Foot**
- **Metal tactile indicators** were present on the floor, particularly before the start of stairs.
- These indicators provide a warning to users about upcoming changes in elevation and help prevent accidents.

**3. Use of Braille**
- Braille was found in:
    - **Floor maps**, assisting in navigation for visually impaired users
    - **Classroom name signs**, helping users identify rooms independently
- This supports improved orientation and accessibility throughout the building.

**4. Inva WC Door Width**
- The door to the accessible (Inva) restroom was **wider than standard toilet doors**.
- It appeared to be **close to compliance with accessibility guidelines in Finland**, allowing better access for wheelchair users.

```
Threshold requirements according to Finnish accessibility guidelines (RakMK F1 and Invalidiliitto):

Clear Opening Width (Home): Minimum 80 cm

Clear Opening Width (Public areas): Minimum 85 cm
```

---

### Conclusion
Overall, the Metropolia premises include several important accessibility features, such as tactile indicators, braille signage, and improved door accessibility for Inva WC facilities. These elements contribute to a more inclusive environment, although further verification (e.g., exact measurements and compliance checks) could strengthen the assessment.