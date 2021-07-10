.text
.globl Main
Main:
jal main
li $v0, 10
syscall
main:
la $t0,a
la $t1,b
la $t2,c
la $t3,d
li $t9,2
sw $t9,($t0)
li $t9,4
sw $t9,($t1)
li $k0,1
lw $k1,($t0)
add $t9,$k1,$k0
sw $t9,($t0)
lw $k0,($t1)
lw $k1,($t0)
mul $t9,$k0,$k1
sw $t9,($t2)
li $k0,9
lw $k1,($t1)
mul $t9,$k0,$k1
li $k0,1
move $k1,$t9
add $t9,$k1,$k0
sw $t9,($t3)
li $v0,1
lw $a0,($t0)
syscall
li $v0,1
lw $a0,($t1)
syscall
li $v0,1
lw $a0,($t2)
syscall
li $v0,1
lw $a0,($t3)
syscall
li $a0, 0
jr $ra
.data
buffer: .space 32
endline: .asciiz "\n"
space: .asciiz " "
a: .word 0
b: .word 0
c: .word 0
d: .word 0
