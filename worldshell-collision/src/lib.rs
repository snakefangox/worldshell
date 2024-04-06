use robusta_jni::bridge;

#[bridge]
mod jni {
    use parry3d_f64::math::{Isometry, Real};
    use parry3d_f64::na::{self, Isometry3, Vector3};
    use parry3d_f64::shape::Cuboid;
    use robusta_jni::convert::{
        Field, IntoJavaValue, Signature, TryFromJavaValue, TryIntoJavaValue,
    };
    use robusta_jni::jni::*;

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(net.snakefangox.worldshell.math)]
    pub struct Vector3d<'env: 'borrow, 'borrow> {
        #[instance]
        raw: objects::AutoLocal<'env, 'borrow>,
        #[field]
        x: Field<'env, 'borrow, f64>,
        #[field]
        y: Field<'env, 'borrow, f64>,
        #[field]
        z: Field<'env, 'borrow, f64>,
    }

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(net.snakefangox.worldshell.math)]
    pub struct Quaternion<'env: 'borrow, 'borrow> {
        #[instance]
        raw: objects::AutoLocal<'env, 'borrow>,
        #[field]
        x: Field<'env, 'borrow, f64>,
        #[field]
        y: Field<'env, 'borrow, f64>,
        #[field]
        z: Field<'env, 'borrow, f64>,
        #[field]
        w: Field<'env, 'borrow, f64>,
    }

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(net.snakefangox.worldshell.collision)]
    pub struct WorldshellCollisionHandler<'env: 'borrow, 'borrow> {
        #[instance]
        raw: objects::AutoLocal<'env, 'borrow>,
    }

    impl<'env: 'borrow, 'borrow> WorldshellCollisionHandler<'env, 'borrow> {
        #[constructor]
        pub extern "java" fn new(env: &'borrow JNIEnv<'env>) -> errors::Result<Self> {}

        #[call_type(safe)]
        pub extern "jni" fn intersects(
            _env: &JNIEnv,
            pos: Vector3d<'env, 'borrow>,
            rotation: Quaternion<'env, 'borrow>,
            halfExtents: Vector3d<'env, 'borrow>,
            blockPos: Vector3d<'env, 'borrow>,
            blockHalfExtents: Vector3d<'env, 'borrow>,
        ) -> errors::Result<bool> {
            let iso = construct_isometry(&pos, Some(&rotation));
            let block_iso = construct_isometry(&blockPos, None);

            let shape = Cuboid::new(convert_vector3(&halfExtents));
            let block_shape = Cuboid::new(convert_vector3(&blockHalfExtents));

            parry3d_f64::query::intersection_test(&iso, &shape, &block_iso, &block_shape)
                .map_err(|_| errors::Error::JavaException)
        }

        #[call_type(safe)]
        pub extern "jni" fn calculateMaxDistance(
            _env: &JNIEnv,
            axis: i8,
            pos: Vector3d<'env, 'borrow>,
            rotation: Quaternion<'env, 'borrow>,
            halfExtents: Vector3d<'env, 'borrow>,
            blockPos: Vector3d<'env, 'borrow>,
            blockHalfExtents: Vector3d<'env, 'borrow>,
            maxDist: f64,
        ) -> f64 {
            let vel = axis_index_to_vectors(axis, maxDist >= 0.0);
            let rot_vel = convert_quat(&rotation) * vel;
            let iso = construct_isometry(&pos, Some(&rotation));
            let block_iso = construct_isometry(&blockPos, None);

            let shape = Cuboid::new(convert_vector3(&halfExtents));
            let block_shape = Cuboid::new(convert_vector3(&blockHalfExtents));

            parry3d_f64::query::time_of_impact(
                &iso,
                &rot_vel,
                &shape,
                &block_iso,
                &Vector3::zeros(),
                &block_shape,
                maxDist.abs(),
                true,
            )
            .map(|t| t.map(|t| t.toi * maxDist.signum()).unwrap_or(maxDist))
            .unwrap_or(maxDist)
        }
    }

    fn construct_isometry<'env, 'borrow>(
        pos: &Vector3d<'env, 'borrow>,
        rotation: Option<&Quaternion<'env, 'borrow>>,
    ) -> Isometry<Real> {
        let mut iso = Isometry3::translation(
            pos.x.get_unchecked(),
            pos.y.get_unchecked(),
            pos.z.get_unchecked(),
        );

        if let Some(r) = rotation {
            let q = convert_quat(r);

            iso.append_rotation_wrt_center_mut(&na::Unit::from_quaternion(*q));
        }

        iso
    }

    fn convert_vector3<'env, 'borrow>(vec: &Vector3d<'env, 'borrow>) -> Vector3<Real> {
        Vector3::new(
            vec.x.get_unchecked(),
            vec.y.get_unchecked(),
            vec.z.get_unchecked(),
        )
    }

    fn convert_quat<'env, 'borrow>(r: &Quaternion<'env, 'borrow>) -> na::UnitQuaternion<Real> {
        na::UnitQuaternion::new_normalize(na::Quaternion::new(
            r.w.get_unchecked(),
            r.x.get_unchecked(),
            r.y.get_unchecked(),
            r.z.get_unchecked(),
        ))
    }

    fn axis_index_to_vectors(idx: i8, positive: bool) -> Vector3<Real> {
        let v = match idx {
            // X
            0 => Vector3::new(1.0, 0.0, 0.0),
            // Y
            1 => Vector3::new(0.0, 1.0, 0.0),
            // Z
            2 => Vector3::new(0.0, 0.0, 1.0),
            // Unless minecraft goes 4D, this is unreachable
            _ => unreachable!(),
        };

        if positive {
            v
        } else {
            -v
        }
    }
}
