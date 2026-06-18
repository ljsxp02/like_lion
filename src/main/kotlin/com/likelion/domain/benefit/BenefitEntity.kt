package com.likelion.domain.benefit

import com.likelion.domain.category.CollegeEntity
import com.likelion.domain.category.DepartmentEntity
import com.likelion.domain.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "benefits")
class BenefitEntity(
    @Column(nullable = false)
    var storeId: Long,

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false)
    var isSchoolWide: Boolean,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Column(nullable = false)
    var isActive: Boolean = true,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "benefit_target_colleges",
        joinColumns = [JoinColumn(name = "benefit_id")],
        inverseJoinColumns = [JoinColumn(name = "college_id")],
    )
    val targetColleges: MutableSet<CollegeEntity> = mutableSetOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "benefit_target_departments",
        joinColumns = [JoinColumn(name = "benefit_id")],
        inverseJoinColumns = [JoinColumn(name = "department_id")],
    )
    val targetDepartments: MutableSet<DepartmentEntity> = mutableSetOf()
}
