.text
.globl main
main:
la $t0,a
la $t1,y
la $t2,n
la $t3,c
la $t4,k
la $t5,f
la $t6,uu
la $s4,io
la $s5,r
la $s6,h
li $t9,0x40200000
mtc1 $t9,$f29
cvt.w.s $f29,$f29
mfc1 $t9,$f29
li $t9,2
sw $t9,($t0)
li $t9,2
sw $t9,($t1)
li $t9,0x40600000
mtc1 $t9,$f29
s.s $f29,($t3)
l.s $f29,($t0)
cvt.s.w $f29,$f29
li $k1,0x40000000
mtc1 $k1,$f31
l.s $f30,($t3)
mul.s $f29,$f30,$f31
s.s $f29,($t2)
la $t8,new_string0
move $t4,$t8
li $v0,8
la $a0,buffer
li $a1,32
move $t9, $a0
syscall
move $t5,$t9
la $t7,new_string1
li $v0,4
move $a0,$t7
syscall
li $v0,1
li $a0,5
syscall
li $v0,4
move $a0,$t4
syscall
li $v0,1
lw $a0,($t0)
syscall
li $t9,0
li $t9,0
sw $t9,($t6)
li $t9,1
li $t9,1
sw $t9,($s4)
lw $t9,($t6)
sw $t9,($s4)
lw $k0,($s4)
lw $k1,($t6)
add $t9,$k0,$k1
bgt $t9,1,label0
ble $t9,1,label1
label0:
addi $t9,$t9,-1
label1:
beqz $t9,st0
bnez $t9,else0
st0:
la $t7,new_string2
li $v0,4
move $a0,$t7
syscall
else0:
b end0
end0:
move $t7,$t5
move $t8,$t4
loop0:
lb $k0, 0($t7)
lb $k1, 0($t8)
sub $t9, $k0, $k1
beqz $t9, continueEqual0
j end_loop0
continueEqual0:
lb $t6, endline
beq $k0, $t6, end_loop0
addi $t7, $t7, 1
addi $t8, $t8, 1
j loop0
end_loop0:
beqz $t9, same0
notSame0:
li $t9,1
j end1
same0:
li $t9,0
end1:
beqz $t9,st1
bnez $t9,else1
st1:
la $t7,new_string3
li $v0,4
move $a0,$t7
syscall
b end2
else1:
la $t7,new_string4
li $v0,4
move $a0,$t7
syscall
b end2
end2:
move $t7,$t5
move $t8,$t4
loop1:
lb $k0, 0($t7)
lb $k1, 0($t8)
sub $t9, $k0, $k1
beqz $t9, continueEqual1
j end_loop1
continueEqual1:
lb $t6, endline
beq $k0, $t6, end_loop1
addi $t7, $t7, 1
addi $t8, $t8, 1
j loop1
end_loop1:
beqz $t9, same1
notSame1:
li $t9,0
j end3
same1:
li $t9,1
end3:
beqz $t9,st2
bnez $t9,else2
st2:
la $t7,new_string5
li $v0,4
move $a0,$t7
syscall
b end4
else2:
la $t7,new_string6
li $v0,4
move $a0,$t7
syscall
b end4
end4:
l.s $f30,($t2)
l.s $f31,($t2)
c.eq.s $f30,$f31
bc1t ,label2
bc1f ,label3
label2:
li $t9,0
b continue0
label3:
li $t9,1
continue0:
beqz $t9,st3
bnez $t9,else3
st3:
la $t7,new_string7
li $v0,4
move $a0,$t7
syscall
else3:
b end5
end5:
l.s $f30,($t2)
l.s $f31,($t2)
c.eq.s $f30,$f31
bc1f label4
bc1t label5
label4:
li $t9,0
b continue1
label5:
li $t9,1
continue1:
beqz $t9,st4
bnez $t9,else4
st4:
la $t7,new_string8
li $v0,4
move $a0,$t7
syscall
else4:
b end6
end6:
l.s $f30,($t2)
l.s $f31,($t2)
c.lt.s $f31,$f30
bc1f label6
bc1t label7
label6:
li $t9,0
b continue2
label7:
li $t9,1
continue2:
beqz $t9,st5
bnez $t9,else5
st5:
la $t7,new_string9
li $v0,4
move $a0,$t7
syscall
else5:
b end7
end7:
l.s $f30,($t2)
l.s $f31,($t2)
c.le.s $f31,$f30
bc1t label8
bc1f label9
label8:
li $t9,0
b continue3
label9:
li $t9,1
continue3:
beqz $t9,st6
bnez $t9,else6
st6:
la $t7,new_string10
li $v0,4
move $a0,$t7
syscall
else6:
b end8
end8:
l.s $f30,($t2)
l.s $f31,($t2)
c.lt.s $f31,$f30
bc1t label10
bc1f label11
label10:
li $t9,0
b continue4
label11:
li $t9,1
continue4:
beqz $t9,st7
bnez $t9,else7
st7:
la $t7,new_string11
li $v0,4
move $a0,$t7
syscall
else7:
b end9
end9:
l.s $f30,($t2)
l.s $f31,($t2)
c.le.s $f31,$f30
bc1f label12
bc1t label13
label12:
li $t9,0
b continue5
label13:
li $t9,1
continue5:
beqz $t9,st8
bnez $t9,else8
st8:
la $t7,new_string12
li $v0,4
move $a0,$t7
syscall
else8:
b end10
end10:
lw $k0,($t0)
lw $k1,($t1)
beq $k0,$k1,label14
bne $k0,$k1,label15
label14:
li $t9,0
b continue6
label15:
li $t9,1
continue6:
beqz $t9,st9
bnez $t9,else9
st9:
la $t7,new_string13
li $v0,4
move $a0,$t7
syscall
else9:
b end11
end11:
lw $k0,($t0)
lw $k1,($t1)
beq $k0,$k1,label16
bne $k0,$k1,label17
label16:
li $t9,1
b continue7
label17:
li $t9,0
continue7:
beqz $t9,st10
bnez $t9,else10
st10:
la $t7,new_string14
li $v0,4
move $a0,$t7
syscall
else10:
b end12
end12:
lw $k0,($t0)
lw $k1,($t1)
bge $k1,$k0,label18
blt $k1,$k0,label19
label18:
li $t9,0
b continue8
label19:
li $t9,1
continue8:
beqz $t9,st11
bnez $t9,else11
st11:
la $t7,new_string15
li $v0,4
move $a0,$t7
syscall
else11:
b end13
end13:
lw $k0,($t0)
lw $k1,($t1)
ble $k1,$k0,label20
bgt $k1,$k0,label21
label20:
li $t9,0
b continue9
label21:
li $t9,1
continue9:
beqz $t9,st12
bnez $t9,else12
st12:
la $t7,new_string16
li $v0,4
move $a0,$t7
syscall
else12:
b end14
end14:
lw $k0,($t0)
lw $k1,($t1)
blt $k1,$k0,label22
bge $k1,$k0,label23
label22:
li $t9,0
b continue10
label23:
li $t9,1
continue10:
beqz $t9,st13
bnez $t9,else13
st13:
la $t7,new_string17
li $v0,4
move $a0,$t7
syscall
else13:
b end15
end15:
lw $k0,($t0)
lw $k1,($t1)
bgt $k1,$k0,label24
ble $k1,$k0,label25
label24:
li $t9,0
b continue11
label25:
li $t9,1
continue11:
beqz $t9,st14
bnez $t9,else14
st14:
la $t7,new_string18
li $v0,4
move $a0,$t7
syscall
else14:
b end16
end16:
l0:
li $k0,5
lw $k1,($t0)
blt $k1,$k0,label26
bge $k1,$k0,label27
label26:
li $t9,0
b continue12
label27:
li $t9,1
continue12:
beqz $t9,l01
bnez $t9,l03
l01:
li $v0,1
lw $a0,($t0)
syscall
li $k0,1
lw $k1,($t0)
add $t9,$k0,$k1
sw $t9,($t0)
li $k0,4
lw $k1,($t0)
beq $k0,$k1,label28
bne $k0,$k1,label29
label28:
li $t9,0
b continue13
label29:
li $t9,1
continue13:
beqz $t9,st15
bnez $t9,else15
st15:
b l03
else15:
b end17
end17:
b l0
l03:
li $k0,3
lw $k1,($t1)
add $t9,$k1,$k0
sw $t9,($t1)
sw $t9,($t1)
li $t9,1
sw $t9,($t1)
l4:
li $k0,6
lw $k1,($t1)
blt $k1,$k0,label30
bge $k1,$k0,label31
label30:
li $t9,0
b continue14
label31:
li $t9,1
continue14:
beqz $t9,l41
bnez $t9,l43
l41:
li $k0,1
lw $k1,($t1)
add $t9,$k1,$k0
sw $t9,($t1)
b l42
l42:
li $k0,0x3f800000
mtc1 $k0,$f30
l.s $f31,($t3)
add.s $f29,$f31,$f30
s.s $f29,($t3)
li $v0, 2
l.s $f12,($t3)
syscall
b l4
l43:
li $t9,1
sw $t9,($t1)
l8:
li $k0,7
lw $k1,($t1)
blt $k1,$k0,label32
bge $k1,$k0,label33
label32:
li $t9,0
b continue15
label33:
li $t9,1
continue15:
beqz $t9,l81
bnez $t9,l83
l81:
li $k0,1
lw $k1,($t1)
add $t9,$k1,$k0
sw $t9,($t1)
b l82
l82:
li $k0,0x3f800000
mtc1 $k0,$f30
l.s $f31,($t3)
sub.s $f29,$f31,$f30
s.s $f29,($t3)
li $v0, 2
l.s $f12,($t3)
syscall
b l8
l83:
la $t7,new_string19
li $v0,4
move $a0,$t7
syscall
la $t8,new_array0
move $s5,$t8
la $t8,new_array1
move $s6,$t8
li $t9,3
li $s1,4
addi $s1,$s1,1
li $s0,4
mul $s1,$s1,$s0
subi $s1,$s1,4
move $t7,$s5
add $t7,$t7,$s1
sw $t9,($t7)
sub $t7,$t7,$s1
move $s5,$t7
li $t9,0x3f800000
li $s1,5
addi $s1,$s1,1
li $s0,4
mul $s1,$s1,$s0
subi $s1,$s1,4
move $t7,$s6
add $t7,$t7,$s1
s.s $f29,($t7)
sub $t7,$t7,$s1
move $s6,$t7
li $t9,0x40133333
li $s1,3
addi $s1,$s1,1
li $s0,4
mul $s1,$s1,$s0
subi $s1,$s1,4
move $t7,$s6
add $t7,$t7,$s1
s.s $f29,($t7)
sub $t7,$t7,$s1
move $s6,$t7
li $k0,0x40000000
li $k1,0x40900000
mtc1 $k0,$f30
mtc1 $k1,$f31
mul.s $f29,$f30,$f31
li $s1,4
addi $s1,$s1,1
li $s0,4
mul $s1,$s1,$s0
subi $s1,$s1,4
move $t7,$s6
add $t7,$t7,$s1
s.s $f29,($t7)
sub $t7,$t7,$s1
move $s6,$t7
li $v0,5
syscall
move $t9,$v0
li $s1,3
addi $s1,$s1,1
li $s0,4
mul $s1,$s1,$s0
subi $s1,$s1,4
move $t7,$s5
add $t7,$t7,$s1
sw $t9,($t7)
sub $t7,$t7,$s1
move $s5,$t7
lw $k0,($t0)
lw $k1,($t0)
xor $t9,$k0,$k1
sw $t9,($t0)
li $v0,1
lw $a0,($t0)
syscall
.data
buffer: .space 32
endline: .asciiz "\n"
space: .asciiz " "
a: .word 0
y: .word 0
n: .float 0.0
c: .float 0.0
k: .asciiz "\n"
f: .asciiz "\n"
uu: .word 0
io: .word 0
r: .word 0:5
h: .float 0:6
new_string0: .asciiz "df\n"
new_string1: .asciiz "dd\n"
new_string2: .asciiz "uu and io is true\n"
new_string3: .asciiz "k equle f\n"
new_string4: .asciiz "k notequle f\n"
new_string5: .asciiz "k notequle f\n"
new_string6: .asciiz "k equle f\n"
new_string7: .asciiz "c equal n\n"
new_string8: .asciiz "c notequal n\n"
new_string9: .asciiz "c biggerequal n\n"
new_string10: .asciiz "c littleequal n\n"
new_string11: .asciiz "c lettle n\n"
new_string12: .asciiz "c bigger n\n"
new_string13: .asciiz "y equal a\n"
new_string14: .asciiz "y notequal a\n"
new_string15: .asciiz "y biggerequal a\n"
new_string16: .asciiz "y littleequal a\n"
new_string17: .asciiz "y lettle a\n"
new_string18: .asciiz "y bigger a\n"
new_string19: .asciiz " \n"
new_array0: .word 0:5
new_array1: .float 0:6
