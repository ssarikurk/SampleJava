@webApp
Feature: Patients Page Tests

  @regression
  Scenario: Patients Billing page - simple navigation/page verification/page structure/data verification test
    When Scenario Started "Patients Billing page - simple navigation/page verification/page structure/data verification test"
    Given the user logged in as "superuser" to "payer_dev" as partner "eft1"
    And select practice location "nacha"
    When the user navigates to Provider "Patients" tab
    And verify title "Patients"
    And verify Patients page header "Patient"
    When the user navigates to Patient ID "9660"
    #And verify the patient's info main page vs detail page
    And the user navigates to the Patients "Billing" page
    And verify the patient's info on Billing page
#    And verify the buttons are enabled on Billing page
#      | Payments |
    And verify the Billing page subheaders
      | Total Account Balance |
      | Aging Balance         |
    And verify Billing page field names
      | Estimate Patient Balance |
      | Insurance Estimate       |
      | Unearned                 |
      | 0-30                     |
      | 31-60                    |
      | 61-90                    |
      | 90+                      |
      | Fees                     |
      | Billed to Insurance      |
      | Allowed Amount           |
      | Insurance Payments       |
      | Adjustments              |
      | Write Off                |
      | Patient Payments         |
    And verify Billing page table headers
      | Date        |
      | Code        |
      | Description |
      | Charges     |
      | Credits     |
      | Balance     |
    And verify the patient's Billing page info on UI
    And verify the Patient ID "9660" Billing page info on UI vs DB with Partner ID "eft1" and collection "procedure_1"

  @regressionRemoved
  Scenario: Patients Billing Payments page - simple navigation/page verification/page structure and data verification test
    When Scenario Started "Patients Billing Payments page - simple navigation/page verification/page structure and data verification test"
    Given the user logged in as "superuser" to "payer_dev" as partner "eft1"
    And select practice location "nacha"
    When the user navigates to Provider "Patients" tab
    And verify Patients page header "Patient"
    When the user navigates to Patient ID "8386"
    #And verify the patient's info main page vs detail page
    And the user navigates to the Patients "Billing" page
    When the user navigates to "Payments" page on Patients Billing page
    Then verify the Payment page header as "Payment"
    And verify the Payment page subheaders
      | ENTER AMOUNT |
      | PAYMENT TYPE |
      | NOTE         |
    And verify the Enter Amount section
      | Amount       |
      | Other Amount |
    And verify the Payment Types
      | Credit Card |
      | Check       |
      | Cash        |
      #| Text-to-Pay       |
      #| Email             |
      | Unearned    |
    And verify the charges types
      | Outstanding    |
      | Treatment Plan |
    And verify the Payment Page Table titles
      | Outstanding Charges    |
      | Current Payment Splits |
    And verify the Payment Page Treatment Plan Table titles
      | Treatment Plan         |
      | Current Payment Splits |
    And verify the Outstanding Charges Table
      | Date        |
      | Code        |
      | Description |
      | AmtOrig     |
      | AmtEnd      |
    And verify the TP Outstanding Charges Table
      | Date        |
      | Code        |
      | Description |
      | AmtOrig     |
      | AmtEnd      |
    And verify the Current Payment Splits Table
      | Date        |
      | Type        |
      | Code        |
      | Description |
      | AmtApply    |
      | AmtEnd      |
    And verify the Payment Page Buttons
      | Clear  |
      | Remove |
      | Pay    |
    And verify the Footer Buttons
      | Cancel           |
      | Complete Payment |
    And verify the Payment Page Footer Labels
      | Total Outstanding Charges    |
      | Total Treatment Plan Charges |
      | Used Credits                 |
      | Remaining Credits            |
    And verify the Patient ID "8386" Billing Payment page info on UI vs DB with Partner ID "eft1" and collection "patient_1" "c9FLtzwBoXH8XhYL"

  @regression @volss
  Scenario: Patients Insurance page - simple navigation/page verification/page structure/static & dynamic data test
    When Scenario Started "Patients Insurance page - simple navigation/page verification/page structure/static & dynamic data test"
    Given the user logged in as "superuser" to "payer_dev" as partner "eft1"
    And select practice location "nacha"
    When the user navigates to Provider "Patients" tab
    And verify Patients page header "Patient"
    When the user navigates to Patient ID "9660"
    #And verify the patient's info main page vs detail page
    When the user navigates to the Patients "Insurance" page
    Then verify the patient's info on the Insurance page
    And verify Patients Insurance page subheaders
      | Insurance Benefits    |
      | Insurance Informations |
      #| Remaining Benefits    |
      #| Insurance Contact     |
    And verify Patients Insurance page Insurance Benefits section
    #And verify Patients Insurance page Total Collectible from Insurance section
    #And verify Patients Insurance page Average Adjustment section
    And verify Patients Insurance page Insurance Information section
      | Insurance Plan ID        |
      | Insurance Name           |
      | Employer                 |
      | Medical Insurance        |
      | Carrier Name             |
      | Carrier Elect ID         |
      | Phone                    |
      | Address                  |
      | Group Name               |
      | Group Num                |
      | BIN                      |
      | Send Electronically      |
      | Subscriber Name          |
      | Subscriber ID            |
      | Subscriber Relationship  |
      | Other Subscribers        |
    And verify the data for Patient ID "9660" on Patients Insurance page UI vs DB with Partner ID "eft1" and collection "claim_proc_1"

  @regressionold
  Scenario: Patients Treatment_Plan page - Page Verification - Page Attachment Headers Verification
    When Scenario Started "Patients Treatment_Plan page - Page Verification - Page Attachment Headers Verification"
    Given the user logged in as "superuser" to "payer_dev" as partner "qa-clinic5"
    And select practice location "1QA-CLINIC5"
#    Given the user logged in as "superuser" to "dev" as partner "processtest1"
    When the user navigates to Provider "Patients" tab
    #And verify Patients page header "Patient"
    When the user navigates to Patient ID "1557"
    #And verify the patient's info main page vs detail page
    And the user navigates to the Patients "Treatment Plan" page
    And Select An Active Treatment Plan
    And Verify Attachment Checklist Fields
    And Open Attachments In This Treatment Plan Section
    And Verify Attachments in This Treatment Plan Fields
      | Tags:X-RayPerio ChartClaim Form |
      | X-RAY Region:MxMdLR             |
    #And Open Treatment Plan Procedure Fields
    # | Tags:X-RayPerio ChartClaim Form |
    # | X-RAY Region:MxMdLR             |
    #And Verify Treatment Plan Procedure Fields
    #  | Claim ID  |
    #  | Claim Key |
    #  | Narrative |


  @regression
  Scenario: Patients - Insurance Settings For Self Paid Patients
    When Scenario Started "Patients - Insurance Settings For Self Paid Patients"
    Given the user logged in as "superuser" to "payer_dev" as partner "pdc-all-chi-api"
    And select practice location "Hershey Family Dentistry"
    And Open Patient Number "8888" Details Page by URL at "lKkYjPP1mO7QpBmh" Practice
    And Open Insurance Settings Tab
#    And Turn "false" Transaction Mapping Switch
#    And Verify Mapping Table "Disable"
#      | 270 - Eligibility                  |
#      | 270B - Benefits                    |
#      | 837D - Claim                       |
#      | 278/Pre-Determination Claim (837D) |
#      | 275                                |
    And Turn "true" Transaction Mapping Switch
    And Verify Mapping Table "Enable"
      | 270 - Eligibility                  |
      | 270B - Benefits                    |
      | 837D - Claim                       |
      | 278/Pre-Determination Claim (837D) |
      | 275                                |
      | 270 - Eligibility                  |
      | 270B - Benefits                    |
      | 837D - Claim                       |
      | 278/Pre-Determination Claim (837D) |
      | 275                                |
    And Turn "false" Transaction Mapping Switch
    And Wait 2 seconds
    And Verify Mapping Table "Disable"
      | 270 - Eligibility                  |
      | 270B - Benefits                    |
      | 837D - Claim                       |
      | 278/Pre-Determination Claim (837D) |
      | 275                                |


