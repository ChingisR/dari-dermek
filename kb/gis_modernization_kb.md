# Дари-дермек — GIS Re-engineering & State Veterinary Drug Registration Modernization Knowledge Base

> **Source Documents:** Interview transcripts and requirements analysis spreadsheets under `d:\!ag\dari\kb`
> **Focus:** Transitioning from paper-based national procedures to EAEU-compliant digital processes in the new State Information System (GIS)
> **Jurisdiction:** Republic of Kazakhstan (RK) within the Eurasian Economic Union (EAEU)
> **Compiled:** July 2026

---

## Table of Contents
1. [Executive Summary & Modernization Goals](#1-executive-summary--modernization-goals)
2. [Re-engineered Registration Process (Target State)](#2-re-engineered-registration-process-target-state)
3. [EAEU Registration Pathways & Timelines](#3-eaeu-registration-pathways--timelines)
4. [GIS Architectural & Functional Requirements](#4-gis-architectural--functional-requirements)
5. [Laboratory Information System (LIS) Workflows](#5-laboratory-information-system-lis-workflows)
6. [Import Control, Supply Chain Traceability, & Antibiotic Tracking](#6-import-control-supply-chain-traceability--antibiotic-tracking)
7. [Market Surveillance & Quality Audits ("Control Purchases")](#7-market-surveillance--quality-audits-control-purchases)
8. [EAEU Registration Timelines Reference Table](#8-eaeu-registration-timelines-reference-table)

---

## 1. Executive Summary & Modernization Goals

The Republic of Kazakhstan is undergoing a comprehensive transition from its national veterinary drug regulation framework to the supranational EAEU regulatory standards (culminating in the December 31, 2030 transition deadline). Historically, the registration process has been heavily paper-based, slow, and fragmented into separate, disconnected services (such as separate applications for laboratory approbation and final state registration).

**Core Goals of the Digital Modernization:**
*   **Consolidation:** Merge the separate approbation and registration filings into a single, unified digital application flow.
*   **Dematerialization:** Convert the manual chancellery paper trail into an end-to-end electronic workflow with automated routing.
*   **Security:** Implement role-based, secure digital lockers for sensitive developer documentation, including manufacturer formulas and strain passports.
*   **Traceability:** Establish full visibility of veterinary drugs from "Point Zero" (border entry or domestic factory output) down to the end users (farms and veterinarians).

---

## 2. Re-engineered Registration Process (Target State)

The new state information system (GIS) will replace the legacy, paper-driven routing (Kancelyariya -> Documentologist -> Department Heads -> Manual review -> Laboratory -> Manual report) with a streamlined, digital sequence:

```
[Applicant: eGov SSO Login]
         │
         ▼
[Submit Application & Digital CTD Dossier]
         │
         ▼
[GIS: Automated Dossier Completeness Check] (10 working days)
         │
         ├─────────────────────────────────────────┐
         ▼ (Pass)                                  ▼ (Fail)
[Applicant: Deliver Samples to NRCV]     [Dossier Rejection / Query]
 (Within 45 working days; Clock Pauses)            │
         │                                         ▼
         ▼                               [Applicant Uploads Fixes]
[NRCV LIS: Run Lab Quality Trials]                 │
 (Microbiology, Chemistry, Clinical)               │
         │                                         │
         ▼                                         │
[LIS: Generate & ECP-Sign Test Protocol]           │
         │                                         │
         ▼                                         │
[NRCV: Generate Final Expert Conclusion] ◄─────────┘
         │
         ▼
[Committee (KVKN): Final Registration Decision]
         │
         ▼
[GIS: Update Unified EAEU Registry & eGov Status]
```

### Key Process Enhancements:
1.  **Single Entry Point:** The applicant log in via eGov SSO (Single Sign-On). No separate "pre-approbation" filing is required.
2.  **Digital Dossier Submission:** The entire EAEU CTD dossier (Parts 1-4) is uploaded electronically.
3.  **Unified Query Resolution:** Any requests for additional information are conducted directly through the applicant's digital portal without aborting and restarting the process. The system clock pauses during queries (up to 90 days, extendable to 180 days).

---

## 3. EAEU Registration Pathways & Timelines

Under the EAEU framework, national registrations will be superseded. The new system supports six primary regulatory procedures:

### 3.1 Bringing into Compliance (Приведение в соответствие)
*   **Purpose:** Transitioning existing national registrations to EAEU-compliant registrations. Must be completed by December 31, 2030.
*   **Timeline:**
    *   Standard drugs: **≤ 90 working days**.
    *   Annex 16.1 group drugs: **≤ 70 working days**.
*   **Key Requirements:** Up-to-date dossier in EAEU format, explanatory memo detailing differences from the original dossier, a pharmacovigilance report (PSUR) covering the last 5 years of commercial circulation, and 3-fold quality control samples.

### 3.2 Standard Registration (Стандартная регистрация)
*   **Purpose:** Registering a brand-new veterinary drug.
*   **Timeline:**
    *   Standard drugs: **≤ 100 working days**.
    *   Annex 8 group drugs: **≤ 95 working days**.
*   **Key Requirements:** Full CTD dossier including pre-clinical toxicity (Annex 14), clinical trials (Annex 15), stability tests, and 3-fold validation batches.

### 3.3 Simplified Registration (Упрощенная регистрация)
*   **Purpose:** Registering reproduced (generic) drugs.
*   **Timeline:**
    *   Standard generics: **≤ 45 working days**.
    *   Annex 8 group generics: **≤ 35 working days**.
*   **Key Requirements:** Proof of bioequivalence or therapeutic equivalence to the reference drug. (Biologicals, biotechnology, and gene-therapy products are barred from this pathway).

### 3.4 Confirmation of Registration (Подтверждение регистрации)
*   **Purpose:** Extending/renewing registration. Initiated before the initial 5-year registration expires.
*   **Timeline:**
    *   Standard drugs: **≤ 40 working days**.
    *   Annex 8 group drugs: **≤ 30 working days**.
*   **Key Requirements:** Review of post-market safety history and pharmacovigilance journals. No sample testing is required.

### 3.5 Dossier Amendments (Внесение изменений)
*   **With Sample Testing:** Modifying active ingredients, manufacturing sites, or key properties that impact quality. **≤ 80–90 working days**.
*   **Without Sample Testing:** Minor changes (packaging design, contact details, secondary suppliers). **≤ 30–40 working days**.

### 3.6 Mutual Recognition (Процедура признания)
*   **Purpose:** Recognizing a drug already registered in another EAEU member state (Reference State).
*   **Timeline:** **≤ 45 working days** for concerned state evaluation.
*   **Key Requirements:** Dossier review and verification of reference state conclusions. No physical samples or lab testing are required.

---

## 4. GIS Architectural & Functional Requirements

The new digital platform must satisfy the following architectural constraints and business needs:

*   **eGov & SSO Integration:** Full authentication using Kazakh state digital signatures (ECP/EDS) and single sign-on.
*   **Role-Based Security & Confidentiality:** Strict segregation of user roles. Sensitive developer IP (such as microbiological strain data, genetic sequences, and proprietary manufacturing formulas) must be stored in encrypted lockers accessible only to assigned experts.
*   **Interstate Data Exchange (EAEU Gateway):** Implement APIs to connect with the Integrated Information System of the Union to push registry updates and verify foreign registrations.
*   **Mobile-First Client Portals:** While expert reviews are conducted on desktops, external operators (such as border inspectors, warehouse clerks, and farmers) must have responsive, mobile-first interfaces for logging transactions and performing lookups.

---

## 5. Laboratory Information System (LIS) Workflows

The laboratory testing phase, executed by the National Reference Center for Veterinary Medicine (NRCV/НРЦВ), is being modernized through LIS automation:

*   **Integrated Testing Teams:** Inside the LIS, testing tasks are automatically routed to corresponding laboratories:
    *   *Microbiologists:* Verifying bacterial/viral properties, strain viability, and sterile conditions.
    *   *Chemical Analysts:* Verifying active pharmaceutical ingredient (API) concentrations, impurities, and excipients.
    *   *Clinical Researchers:* Verifying target animal safety and immune responses.
*   **Collaborative Protocol Drafting:** Specialists draft their respective sections of the test protocol within the LIS.
*   **Digital Signatures (ECP):** The final test report is digitally signed by the individual analysts, department heads, and the Deputy General Director, removing the need for physical printing and physical routing.
*   **Out-of-Laboratory Audits:** In cases where specific testing reagents or reference standards are unavailable locally, the LIS supports logging and scheduling on-site expert evaluations at the manufacturer's laboratory.

---

## 6. Import Control, Supply Chain Traceability, & Antibiotic Tracking

To combat the illicit distribution of veterinary pharmaceuticals and control the "grey market" of antibiotics, the GIS introduces strict tracking from entry to use:

*   **Customs & Border Parking:** Import shipments arriving at EAEU checkpoints are cross-referenced with the GIS registration database. Unregistered drugs are automatically flagged and routed to customs custody/parking.
*   **Volume & Destination Balancing:** Importers must declare the batch volume (number of doses/vials) and the target destination (e.g. specific warehouse or wholesale distributor). The system balances these volumes to detect leakages (e.g., if a 100-liter batch clears customs but only 50 liters are logged at the warehouse, a flag is raised).
*   **Vial QR-Code Traceability:** Vials and packaging are marked with unique serial numbers/QR codes. Inspectors, veterinarians, and farmers scan these codes with mobile devices to verify:
    *   Registration status.
    *   Batch origin and manufacturing date.
    *   Cold-chain maintenance records.
*   **Antibiotic Supervision:** Antibiotics are subject to strict prescribing controls. The GIS registers veterinary prescriptions and monitors inventory levels to prevent the unregistered prophylactic use of reserve human antibiotics in livestock.

---

## 7. Market Surveillance & Quality Audits ("Control Purchases")

To ensure ongoing drug quality post-registration, the Committee conducts random surveillance audits:

*   **Random Market Sampling:** Committee inspectors perform random "control purchases" (контрольный закуп) of active veterinary drug batches from pharmacies, warehouses, or distributors.
*   **NRCV Quality Verification:** Collected samples are dispatched to NRCV laboratories for blinded quality verification.
*   **Funding:** The laboratory testing is funded at the MAH/applicant's expense under statutory control provisions.
*   **Enforcement Actions:** If a sample fails the laboratory criteria:
    *   The entire batch is immediately locked and suspended in the GIS registry.
    *   A notification is pushed to all distributors and warehouses to freeze inventory.
    *   The MAH must recall, destroy (via chemical denaturation under commission rules), or replace the batch at their own expense.

---

## 8. EAEU Registration Timelines Reference Table

The table below outlines the maximum timelines and sample testing requirements for EAEU procedures in the re-engineered GIS:

| # | Procedure | Description | Max Timeline (Working Days) | Sample Testing Required |
|---|---|---|---|---|
| **1** | Bringing into Compliance | Transitioning national registrations to EAEU | **70 – 90 days** | Yes (At reference authority's request) |
| **2** | Standard Registration | Registering a brand-new vet drug | **95 – 100 days** | Yes (3-fold quality control volume) |
| **3** | Simplified Registration | Registering generic drugs | **35 – 45 days** | Yes (Bioequivalence/quality control) |
| **4** | Confirmation | Extending registration after 5 years | **30 – 40 days** | No (Review of safety history only) |
| **5** | Changes (With Testing) | Major modifications to composition/manufacturing | **80 – 90 days** | Yes (To verify impact of changes) |
| **6** | Changes (No Testing) | Administrative or minor packaging updates | **30 – 40 days** | No |
| **7** | Recognition | Accepting registration from another EAEU state | **45 days** | No (Dossier review only) |
