.DATA
	; Variables globales (si las hubiera)
.TEXT
	JUMP main

; [LABEL, factorial, null, null]
factorial:

; [PARAM_IN, 0, null, n]
	; (Directiva: PARAM_IN)

; [<, n, 1, t0]
	LOAD R1, [SP + 0]
	LOADI R2, #1
	CMP R1, R2
	JUMPLT L_TRUE_4
	LOADI R0, #0
	JUMP L_END_4
L_TRUE_4:
	LOADI R0, #1
L_END_4:
	STORE R0, [SP + 4]

; [JUMPIF_FALSE, t0, null, L1]
	LOAD R1, [SP + 4]
	CMP R1, #0
	JUMPEQ L1

; [RETURN, 1, null, null]
	LOADI R0, #1
	RET

; [GOTO, L0, null, null]
	JUMP L0

; [GOTO, L2, null, null]
	JUMP L2

; [LABEL, L1, null, null]
L1:

; [-, n, 1, t1]
	LOAD R1, [SP + 0]
	LOADI R2, #1
	SUB R0, R1, R2
	STORE R0, [SP + 8]

; [PARAM, t1, null, null]
	LOAD R1, [SP + 8]
	PUSH R1

; [CALL, factorial, 1, t2]
	CALL factorial
	STORE R0, [SP + 12]

; [*, n, t2, t3]
	LOAD R1, [SP + 0]
	LOAD R2, [SP + 12]
	MUL R0, R1, R2
	STORE R0, [SP + 16]

; [ASSIGN, t3, null, temp]
	LOAD R1, [SP + 16]
	STORE R1, [SP + 20]

; [RETURN, temp, null, null]
	LOAD R0, [SP + 20]
	RET

; [GOTO, L0, null, null]
	JUMP L0

; [LABEL, L2, null, null]
L2:

; [LABEL, L0, null, null]
L0:

; [END_FUNCTION, null, null, null]
	; (Directiva: END_FUNCTION)

; [LABEL, main, null, null]
main:

; [ASSIGN, 5, null, x]
	LOADI R1, #5
	STORE R1, [SP + 24]

; [LABEL, L4, null, null]
L4:

; [>, x, 0, t4]
	LOAD R1, [SP + 24]
	LOADI R2, #0
	CMP R1, R2
	JUMPGT L_TRUE_28
	LOADI R0, #0
	JUMP L_END_28
L_TRUE_28:
	LOADI R0, #1
L_END_28:
	STORE R0, [SP + 28]

; [JUMPIF_FALSE, t4, null, L5]
	LOAD R1, [SP + 28]
	CMP R1, #0
	JUMPEQ L5

; [PARAM, x, null, null]
	LOAD R1, [SP + 24]
	PUSH R1

; [CALL, factorial, 1, t5]
	CALL factorial
	STORE R0, [SP + 32]

; [ASSIGN, t5, null, resultado]
	LOAD R1, [SP + 32]
	STORE R1, [SP + 36]

; [-, x, 1, t6]
	LOAD R1, [SP + 24]
	LOADI R2, #1
	SUB R0, R1, R2
	STORE R0, [SP + 40]

; [ASSIGN, t6, null, x]
	LOAD R1, [SP + 40]
	STORE R1, [SP + 24]

; [GOTO, L4, null, null]
	JUMP L4

; [LABEL, L5, null, null]
L5:

; [ASSIGN, 120, null, resultado]
	LOADI R1, #120
	STORE R1, [SP + 36]

; [RETURN, resultado, null, null]
	LOAD R0, [SP + 36]
	RET

; [GOTO, L3, null, null]
	JUMP L3

; [LABEL, L3, null, null]
L3:

; [END_FUNCTION, null, null, null]
	; (Directiva: END_FUNCTION)
