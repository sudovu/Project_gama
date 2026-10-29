DO $reconcile$
DECLARE
    v_terminal_status text;
    v_repair_status_val "RepairStatus";
BEGIN
    -- 1. Introspect valid terminal state from pg_enum dynamically
    SELECT e.enumlabel INTO v_terminal_status
    FROM pg_type t
    JOIN pg_enum e ON t.oid = e.enumtypid
    WHERE t.typname = 'RepairStatus'
      AND e.enumlabel IN ('DELIVERED', 'READY_FOR_DELIVERY', 'READY_FOR_PICKUP', 'RESOLVED', 'CLOSED', 'DONE', 'COMPLETED')
    ORDER BY CASE 
        WHEN e.enumlabel = 'DELIVERED' THEN 1
        WHEN e.enumlabel = 'READY_FOR_DELIVERY' THEN 2
        WHEN e.enumlabel = 'READY_FOR_PICKUP' THEN 3
        WHEN e.enumlabel = 'RESOLVED' THEN 4
        WHEN e.enumlabel = 'DONE' THEN 5
        ELSE 6
    END
    LIMIT 1;

    -- Fallback to the latest sort order if none of the above matched
    IF v_terminal_status IS NULL THEN
        SELECT e.enumlabel INTO v_terminal_status
        FROM pg_type t
        JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'RepairStatus'
        ORDER BY e.enumsortorder DESC
        LIMIT 1;
    END IF;

    RAISE NOTICE 'Target RepairStatus selected: %', v_terminal_status;

    -- 2. Synchronize Invoice record with authoritative Ledger (NPR 8,475.00)
    UPDATE "Invoice"
    SET 
        "subTotal"   = 7500.00,
        "taxAmount"  = 975.00,
        "grandTotal" = 8475.00,
        "paidAmount" = 8475.00,
        "isPaid"     = TRUE,
        "notes"      = 'Reconciled: Samsung 980 NVMe SSD (5,800.00) + Workshop Labor (1,700.00) + 13% VAT (975.00)'
    WHERE "invoiceNo" = 'INV-20261028-0005';

    -- 3. Update RepairJob with introspected enum value
    EXECUTE format('
        UPDATE "RepairJob"
        SET 
            "status"         = %L::"RepairStatus",
            "finalLaborCost" = 1700.00,
            "finalPartsCost" = 5800.00,
            "totalCost"      = 7500.00,
            "completedDate"  = CURRENT_TIMESTAMP,
            "qcPassed"       = TRUE,
            "qcNotes"        = %L
        WHERE "jobNumber" = %L',
        v_terminal_status,
        'Storage upgrade verified. NVMe read/write benchmark passed. OS activated.',
        'JOB-20261028-0005'
    );
END $reconcile$;
