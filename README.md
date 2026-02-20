
# Solar Charge Controller Calculator - Xantrex

## Team Member Roles
- **Anas**
- **Greg** – Writeup Overview
- **Erfan** – Group Meeting Summaries / Specific Requirements / Epics
- **Anton** – Deliverables
- **Huda** – Introduction / Functional Epics

---

## 1. Introduction
This project proposes the development of a Solar MPPT Controller Calculator that determines the correct MPPT solar charge controller based on user-provided solar panel specifications, environmental conditions, and battery bank voltage. The system ensures compliance with NEC standards and manufacturer safety requirements.

## 2. Project Objectives
- Accurately calculate solar array electrical parameters
- Apply National Electrical Code (NEC) Table 690.7(A) temperature correction factors to open circuit voltage
- Filter MPPT controllers based on battery bank voltage compatibility
- Select a controller rated appropriately for voltage and current requirements
- Ensure short circuit current safety validation based on manufacturer datasheets (Victron's Datasheets)

---

## 3. Solar Array Calculations

### 3.1 Total Power
**Formula:**
```
Total Power = PMAX × Number of Panels
```

### 3.2 Open Circuit Voltage (Corrected)
**Formula:**
```
Open Circuit Voltage = VOC × Panels in Series × Temperature Factor
```
*Temperature Factor is based on NEC Table 690.7(A) for -13°F (-25°C)*

### 3.3 Maximum Charge Current
**Formula:**
```
Max Charge Current = Total Power ÷ Battery Charge Voltage
```
**Battery Charge Voltage:**
- 14.7V for 12V battery bank
- 29.4V for 24V battery bank

### 3.4 Short Circuit Current
**Formula:**
```
Short Circuit Current = ISC (victron datasheet) × Panels in Parallel
```
![Solar Diagram](resources/image.png)
---

## 4. Functional and Required Epics

### Epic 1: Solar Array Calculation Engine
**Goal:**
> Develop backend logic to compute Total Power, Corrected Open Circuit Voltage, Maximum Charge Current, and Short Circuit Current based on user inputs.

**Description:**
Use Solar Array Calculation (Section 3) for computation.

**Value:**
Accurate electrical calculations ensure proper MPPT controller sizing and prevent system overload or unsafe installations.

**Acceptance Criteria:**
- User inputs PMAX, VOC, ISC
- User specifies the number of panels in series and parallel
- Temperature factor is applied automatically
- Battery bank voltage (12V or 24V) affects charge current calculation
- All computed values are displayed clearly

---

### Epic 2: NEC Temperature Correction Implementation
**Goal:**
> Apply NEC-compliant voltage correction factors to ensure safe controller sizing under cold temperature conditions.

**Description:**
- Users input their regional temperature
- Apply Temperature Correction Factor (T°Factor) based on -13°F (-25°C)
- Use voltage correction values from NEC Table 690.7(A)
- Automatically adjust VOC for worst-case cold temperature conditions

**Value:**
Ensures electrical safety and regulatory compliance.

**Acceptance Criteria:**
- System includes NEC-based correction factors
- Open Circuit Voltage is multiplied by T°Factor
- Calculation reflects cold-climate voltage rise
- Factors can be updated if standards change (future iteration)

---

### Epic 3: MPPT Controller Compatibility Filtering
**Goal:**
> Filter available MPPT controllers based on battery bank voltage (12V or 24V).

**Description:**
- Filter controllers compatible with:
	- 12V battery bank
	- 24V battery bank
- Only display MPPT controllers supporting the selected voltage system

**Value:**
Prevents incompatible controller selection and reduces configuration errors.

**Acceptance Criteria:**
- User selects battery bank voltage (12V or 24V)
- Controller list updates accordingly
- Incompatible controllers are excluded

---

### Epic 4: Controller Sizing Logic
**Goal:**
> Select the closest MPPT controller rated above both calculated Open Circuit Voltage and Maximum Charge Current.

**Description:**
- Compare calculated Open Circuit Voltage and Max Charge Current
- Select the closest MPPT controller rated above or equal to both values
- Prefer the smallest compatible controller (closest match)

**Business Logic:**
- Controller VOC<sub>rating</sub> ≥ Array Open Circuit Voltage  
	(The controller must be able to handle at least as much voltage as the solar panels can produce.)
- Controller Current<sub>rating</sub> ≥ Max Charge Current  
	(The controller must be able to handle at least as much current as the system will send into the battery.)

**Value:**
Optimizes cost while maintaining safety.

**Acceptance Criteria:**
- System evaluates both voltage and current ratings
- Only controllers meeting both thresholds are shown
- The closest-rated match is selected

---

### Epic 5: Short Circuit Current Safety Validation
**Goal:**
> Verify the selected controller supports the calculated Short Circuit Current as per manufacturer datasheets (Victron Datasheet Compliance).

**Description:**
- Compare calculated Short Circuit Current
- Validate it against the controller’s maximum PV short circuit current rating (per manufacturer datasheet)
- Exclude controllers that fail this safety requirement

**Business Rule:**
- Controller ISC<sub>rating</sub> ≥ Array Short Circuit Current  
	(The controller must be able to safely handle the maximum short-circuit current coming from the solar panels.)

**Value:**
Prevents controller damage and ensures manufacturer compliance.

**Acceptance Criteria:**
- Short Circuit Current is calculated
- Controller’s ISC rating is verified
- Incompatible controllers are removed
- Final recommendation meets voltage, current, and ISC requirements

---

### Epic 6: Admin Authentication and Controller Database Management
**Goal:**
> Provide a secure administrative interface for managing the Xantrex controller database used by the calculator.

**Description:**
- Require admin login with authenticated credentials to access the management dashboard
- Allow administrators to perform full CRUD operations on the controller database:
	- **Create:** Add new Xantrex controllers with voltage rating, current rating, ISC rating, and battery bank compatibility
	- **Read:** View all controllers currently in the database
	- **Update:** Edit specifications of existing controllers (e.g., updated ratings, new model revisions)
	- **Delete:** Remove discontinued or obsolete controllers from the database

**Value:**
Allows authorized personnel to maintain an accurate and up-to-date controller catalog without requiring code changes, ensuring the calculator always recommends current Xantrex products.

**Acceptance Criteria:**
- Admin must log in with valid credentials to access the dashboard
- Unauthorized users cannot access the management interface
- Admin can add, view, edit, and delete controller entries
- The public calculator uses the updated controller data in real time
- Input validation prevents incomplete or invalid controller entries

---

### Epic 7: Weather API Integration and Automatic Temperature Correction
**Goal:**
> Automatically retrieve location-based temperature data via a REST API to apply the correct voltage correction factor for the user's region.

**Description:**
- Allow the user to input their city or region
- Send a request to the OpenWeatherMap API to retrieve the historical minimum temperature for that location
- Map the retrieved temperature to the appropriate voltage correction factor
- Automatically apply the correction factor to the Open Circuit Voltage calculation
- Display the retrieved temperature and applied correction factor to the user for transparency
- If the API fails, ask for the minimum and maximum temperature of the region manually

**Value:**
Provides location-specific voltage correction rather than assuming a fixed worst-case temperature, resulting in more accurate and cost-effective controller recommendations tailored to the user's actual climate.

**Acceptance Criteria:**
- User can input their city or region
- System successfully calls the OpenWeatherMap API and retrieves temperature data
- The correct correction factor is selected based on the retrieved temperature
- The correction factor is applied to the Open Circuit Voltage calculation automatically
- If the API call fails, the system asks the user for a maximum and minimum temperature in their region
- The user can see what temperature and correction factors were applied

---

## 5. System Workflow
1. User inputs PMAX, VOC, ISC, number of panels in series and parallel
2. The user selects battery bank voltage (12V or 24V)
3. The system calculates all required electrical parameters
4. Controllers are filtered based on battery compatibility
5. The system selects controller meeting voltage, current, and short circuit requirements
6. The closest compatible MPPT controller is displayed

---

## 6. Technical Terms

### 6.1 Temperature Correction Factor (T° Factor)
A multiplier used to increase voltage calculations in cold weather. Cold temperatures increase solar panel voltage.
*Comes from: NEC Table 690.7(A)*

### 6.2 NEC (National Electrical Code)
A safety rulebook for electrical installations in North America. It ensures safe wiring, proper equipment sizing, and fire prevention.
*In This Project: Used for voltage correction in cold climates.*

### 6.3 Battery Charge Voltage
The voltage required to properly charge a battery.

### 6.4 Max Charge Current
The maximum current that flows from the controller into the battery during charging.

### 6.5 MPPT (Maximum Power Point Tracking)
A type of solar charge controller that optimizes power from panels and adjusts voltage to maximize efficiency.

### 6.6 Controller Voltage Rating
The maximum solar panel voltage the controller can safely handle.

### 6.7 Controller Current Rating
The maximum charging current the controller can deliver to the battery.

### 6.8 Controller ISC Rating
The maximum short-circuit current the controller can tolerate from the solar array.

---

## 7. Meeting Summaries

### Meeting 1 – January 22, 2025 (In-Person)
Our first group meeting served as an introduction where all five members got to know each other. Each member shared their background in computer science, including previous coursework, programming languages they are comfortable with, and areas of strength. This helped the group understand what each person can contribute and laid the groundwork for assigning roles in future iterations.

### Meeting 2 – February 10, 2025 (In-Person)
The group met to review the project requirements outlined in the proposal assignment. We discussed the deliverables, marking criteria, and the expectations for each iteration. We also prepared a list of questions to bring to our first meeting with the client, covering topics such as user type, authentication needs, database requirements, API usage, integration with the existing Xantrex website, and desired features.

### Meeting 3 – February 11, 2025 (Client Meeting with Ling Weng and Angela Shi)
We met with our clients Ling Weng and Angela Shi from Xantrex. Key takeaways from the meeting included that the calculator is intended for end customers rather than internal staff, so no login is required from Xantrex's perspective. The core functionality involves the user inputting solar panel specifications and receiving a recommended Xantrex controller along with a generated wiring diagram. The client emphasized ease of use and consistency with the existing Xantrex website design. Location-based temperature data was discussed as an important factor for accurate calculations, with maximum temperature being the primary input and minimum temperature being optional but desirable. The client also mentioned the possibility of redirecting users to a purchase page after receiving their result. The group was tasked with researching the calculation standards and coming up with the logic independently.

### Meeting 4 – February 17, 2025 (Online)
The group met to divide the proposal workload among members. Roles were assigned as follows: Greg is handling the writeup overview, Erfan is responsible for group meeting summaries and mandatory requirements, Anton is managing deliverables, and Huda is writing the introduction and functional epics. We are planning to meet again on either February 24 or 26 after class to discuss roles and tasks for iteration 1.

